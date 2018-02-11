package cs.ut.ui.controllers.validation

import cs.ut.charts.Chart
import cs.ut.charts.ChartGenerator
import cs.ut.charts.MAE
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.ui.adapters.JobValueAdataper
import cs.ut.ui.controllers.Redirectable
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_MODELS_OVERVIEW
import cs.ut.util.PAGE_TRAINING
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.event.SerializableEventListener
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Cell
import org.zkoss.zul.Combobox
import org.zkoss.zul.Comboitem
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Rows
import org.zkoss.zul.Vbox

class SingleJobValidationController : SelectorComposer<Component>(), Redirectable {
    private val log = NirdizatiLogger.getLogger(SingleJobValidationController::class.java)
    private lateinit var job: SimulationJob
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
    lateinit var infoContainer: Vbox

    @Wire
    lateinit var comparisonContainer: Vbox

    private var currentlySelected: String = ""

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        job = Executions.getCurrent().getAttribute(JobValueAdataper.jobArg) as SimulationJob
        log.debug("Received job argument $job, initializing in read only mode")
        charts = ChartGenerator(job).getCharts().groupBy { it.javaClass.name }
        generateReadOnlyMode()
    }

    @Listen("onClick=#backToTraining")
    fun backToTraining() {
        setContent(PAGE_TRAINING, page)
    }

    @Listen("onClick=#backToValidation")
    fun backToValidation() {
        setContent(PAGE_MODELS_OVERVIEW, page)
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

        cell.addEventListener(Events.ON_CLICK, { _ ->
            selectionRows.getChildren<Row>().first().getChildren<Cell>().forEach { it.sclass = "" }
            cell.sclass = "selected-option"
            currentlySelected = entry.key
        })

        cell.addEventListener(
            Events.ON_CLICK,
            if (entry.value.size == 1) entry.value.first().generateListenerForOne() else entry.value.generateListenerForMany()
        )
        cell.appendChild(label)
        this.appendChild(cell)
    }

    private fun Chart.generateListenerForOne(): SerializableEventListener<Event> {
        return SerializableEventListener { _ ->
            removeChildren()
            comboLayout.isVisible = false
            this.render()
            setVisibility()
        }
    }

    private fun setVisibility() {
        comparisonContainer.isVisible = currentlySelected == "cs.ut.charts.LineChart"
        infoContainer.isVisible = comparisonContainer.isVisible && comboLayout.isVisible
    }

    private fun removeChildren() {
        comboLayout.getChildren<Component>().clear()
    }

    private fun List<Chart>.generateListenerForMany(): SerializableEventListener<Event> {
        return SerializableEventListener { _ ->
            removeChildren()

            comboLayout.isVisible = true
            setVisibility()

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
        job.learner.properties.forEach { this.generateLabelAndValue("property." + it.id, it.property, false) }
    }

    private fun Row.generateMainData() {
        var param = job.encoding
        generateLabelAndValue(param.type, param.type + "." + param.id)
        param = job.bucketing
        generateLabelAndValue(param.type, param.type + "." + param.id)
        param = job.learner
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