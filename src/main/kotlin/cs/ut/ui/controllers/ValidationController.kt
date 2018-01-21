package cs.ut.ui.controllers

import cs.ut.charts.Chart
import cs.ut.charts.ChartGenerator
import cs.ut.charts.MAE
import cs.ut.jobs.SimulationJob
import cs.ut.ui.adapters.JobValueAdataper
import cs.ut.util.NirdizatiUtil
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.event.SerializableEventListener
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.*

class ValidationController : SelectorComposer<Component>() {
    private val log = Logger.getLogger(ValidationController::class.java)
    private var job: SimulationJob? = null
    private lateinit var charts: Map<String, List<Chart>>

    @Wire
    private lateinit var metadataRows: Rows

    @Wire
    private lateinit var selectionRows: Rows

    @Wire
    private lateinit var propertyRows: Rows

    @Wire
    private lateinit var comboLayout: Vbox

    @Wire
    private lateinit var logSelectionGrid: Grid

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        job = Executions.getCurrent().getAttribute(JobValueAdataper.jobArg) as SimulationJob?
        job?.let {
            log.debug("Received job argument $job, initializing in read only mode")
            charts = ChartGenerator(job as SimulationJob).getCharts().groupBy { it.javaClass.name }
            generateReadOnlyMode()
            return
        }

        log.debug("Inital job not found -> initializing valdiation mdoe withtout context")
        logSelectionGrid.isVisible = true
        generateCombo()
    }

    private fun generateCombo() {
        val combo: Combobox = Rows().let {
            logSelectionGrid.appendChild(it)
            val combo = Combobox()
            it.appendChild(Row().also {
                it.align = "center"
                it.appendChild(Hlayout().also {
                    it.appendChild(Label(NirdizatiUtil.localizeText("validation.select_completed")).also { it.sclass = "param-label" })
                    it.appendChild(combo)
                })
            })
            combo
        }
        log.debug("Generated layout for no context mode")

        combo.addEventListener(Events.ON_SELECT, { e ->
            log.debug("Generating layout for new job -> ${e.data}")
            job = e.data as SimulationJob
            charts = ChartGenerator(job!!).getCharts().groupBy { it.javaClass.name }
            generateReadOnlyMode()
        })
    }

    private fun generateReadOnlyMode() {
        generateMetadataRow()
        generatePropertyRow()
        generateChartOptions()
    }

    private fun generateChartOptions() {
        val row = Row()
        row.align = "center"
        charts.forEach { row.generateCell(it) }
        selectionRows.appendChild(row)
        Events.postEvent("onClick", row.getChildren<Component>().first { it.id == "cs.ut.charts.LineChart" }, null)
    }

    private fun Row.generateCell(entry: Map.Entry<String, List<Chart>>) {
        val cell = Cell()
        val label = Label(NirdizatiUtil.localizeText(entry.key))
        cell.id = entry.key
        cell.align = "center"
        cell.valign = "center"
        cell.addEventListener(
                Events.ON_CLICK,
                if (entry.value.size == 1) entry.value.first().generateListenerForOne() else entry.value.generateListenerForMany()
        )
        cell.addEventListener(Events.ON_CLICK, { _ ->
            selectionRows.getChildren<Row>().first().getChildren<Cell>().forEach { it.sclass = "" }
            cell.sclass = "selected-option"
        })
        cell.appendChild(label)
        this.appendChild(cell)
    }

    private fun Chart.generateListenerForOne(): SerializableEventListener<Event> {
        return SerializableEventListener { _ ->
            removeChildren()
            this.render()
        }
    }

    private fun removeChildren() {
        comboLayout.getChildren<Component>().clear()
    }

    private fun List<Chart>.generateListenerForMany(): SerializableEventListener<Event> {
        return SerializableEventListener { _ ->
            removeChildren()

            val label = Label(Labels.getLabel("validation.select_version"))
            label.sclass = "param-label"
            comboLayout.appendChild(label)

            var itemSet = false
            val combobox = Combobox()
            this.forEach {
                val comboItem = combobox.appendItem(NirdizatiUtil.localizeText(it.getCaption()))
                comboItem.setValue(it)

                if (MAE == it.name) {
                    combobox.selectedItem = comboItem
                    itemSet = true
                }
            }

            if (!itemSet) combobox.selectedItem = combobox.items.first()

            combobox.isReadonly = true
            combobox.setConstraint("no empty")
            (combobox.selectedItem.getValue() as Chart).render()
            combobox.width = "330px"

            combobox.addEventListener(
                    Events.ON_SELECT,
                    { e -> (((e as SelectEvent<*, *>).selectedItems.first() as Comboitem).getValue() as Chart).render() })
            comboLayout.appendChild(combobox)
        }
    }


    private fun generatePropertyRow() {
        val row = Row()
        row.align = "center"
        row.generatePropertyData()
        propertyRows.appendChild(row)
    }

    private fun generateMetadataRow() {
        val row = Row()
        row.align = "center"
        row.generateMainData()
        metadataRows.appendChild(row)
    }

    private fun Row.generatePropertyData() {
        job!!.learner.properties.forEach { this.generateLabelAndValue("property." + it.id, it.property, false) }
    }

    private fun Row.generateMainData() {
        var param = job!!.encoding
        generateLabelAndValue(param.type, param.type + "." + param.id)
        param = job!!.bucketing
        generateLabelAndValue(param.type, param.type + "." + param.id)
        param = job!!.learner
        generateLabelAndValue(param.type, param.type + "." + param.id)
    }

    private fun Row.generateLabelAndValue(labelCaption: String, valueCaption: String, localizeValue: Boolean = true) {
        val label = Label(Labels.getLabel(labelCaption) + ": ")
        val value = Label(if (localizeValue) Labels.getLabel(valueCaption) else valueCaption)
        value.sclass = "param-label"

        val hlayout = Hlayout()
        hlayout.appendChild(label)
        hlayout.appendChild(value)
        this.appendChild(hlayout)
    }
}