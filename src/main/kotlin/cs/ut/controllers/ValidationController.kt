package cs.ut.controllers

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
import org.zkoss.zul.Cell
import org.zkoss.zul.Combobox
import org.zkoss.zul.Comboitem
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Rows
import org.zkoss.zul.Vbox

class ValidationController : SelectorComposer<Component>() {
    private val log = Logger.getLogger(ValidationController::class.java)
    private var job: SimulationJob? = null
    private val charts: Map<String, List<Chart>> by lazy { ChartGenerator(job as SimulationJob).getCharts().groupBy { it.javaClass.name } }

    @Wire
    lateinit private var metadataRows: Rows

    @Wire
    lateinit private var selectionRows: Rows

    @Wire
    lateinit private var propertyRows: Rows

    @Wire
    lateinit private var comboLayout: Vbox

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        job = Executions.getCurrent().getAttribute(JobValueAdataper.jobArg) as SimulationJob?
        job?.let {
            log.debug("Received job argument $job, initializing in read only mode")
            generateReadOnlyMode()
            return
        }

        TODO("Validation without context not implemented yet")
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
        val label = Label(Labels.getLabel(entry.key))
        cell.id = entry.key
        cell.align = "center"
        cell.valign = "center"
        cell.addEventListener(Events.ON_CLICK,
                if (entry.value.size == 1) entry.value.first().generateListenerForOne() else entry.value.generateListenerForMany())
        cell.addEventListener(Events.ON_CLICK, { _ ->
            selectionRows.getChildren<Row>().first().getChildren<Cell>().forEach { it.setClass("") }
            cell.setClass("selected-option")
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
            label.style = "font-weight: bold;"
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

            combobox.addEventListener(Events.ON_SELECT, { e -> (((e as SelectEvent<*, *>).selectedItems.first() as Comboitem).getValue() as Chart).render() })
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
        value.style = "font-weight: bold"

        val hlayout = Hlayout()
        hlayout.appendChild(label)
        hlayout.appendChild(value)
        this.appendChild(hlayout)
    }
}