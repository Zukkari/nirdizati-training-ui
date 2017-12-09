package cs.ut.controllers

import cs.ut.charts.Chart
import cs.ut.charts.ChartGenerator
import cs.ut.jobs.SimulationJob
import cs.ut.ui.providers.JobValueProvider
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
    private val charts: Map<String, List<Chart>> by lazy { ChartGenerator(job).getCharts().groupBy { it.javaClass.name } }

    @Wire
    lateinit private var metadataRows: Rows

    @Wire
    lateinit private var selectionRows: Rows

    @Wire
    lateinit private var propertyRows: Rows

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        job = Executions.getCurrent().getAttribute(JobValueProvider.jobArg) as SimulationJob?
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
        charts.forEach { generateCell(row, it) }
        selectionRows.appendChild(row)
    }

    private fun generateCell(row: Row, entry: Map.Entry<String, List<Chart>>) {
        val cell = Cell()
        val label = Label(Labels.getLabel(entry.key))
        cell.align = "center"
        cell.valign = "center"
        cell.addEventListener(Events.ON_CLICK,
                if (entry.value.size == 1) generateListenerForOne(entry.value.first()) else generateListenerForMany(entry.value, entry.key))
        cell.addEventListener(Events.ON_CLICK, { _ ->
            selectionRows.getChildren<Row>().first().getChildren<Cell>().forEach { it.setClass("") }
            cell.setClass("selected-option")
        })
        cell.appendChild(label)
        row.appendChild(cell)
    }

    private fun generateListenerForOne(chart: Chart): SerializableEventListener<Event> {
        return SerializableEventListener { _ ->
            removeChildren()
            chart.render()
        }
    }

    private fun removeChildren() {
        val children = selectionRows.getChildren<Row>()
        while (children.size > 1) selectionRows.removeChild(children.last())
    }

    private fun generateListenerForMany(charts: List<Chart>, key: String): SerializableEventListener<Event> {
        fun populateRow(row: Row): Cell {
            val keys = this.charts.keys.toList()

            for (i in 0 until keys.indexOf(key)) {
                row.appendChild(Cell())
            }

            val cell = Cell()
            row.appendChild(cell)

            for (i in 0..keys.size - (keys.indexOf(key) + 2)) {
                row.appendChild(Cell())
            }

            return cell
        }

        return SerializableEventListener { _ ->
            removeChildren()
            val hlayout = Hlayout()
            hlayout.appendChild(Label(Labels.getLabel("validation.select_version")))

            val combobox = Combobox()
            charts.forEach {
                val comboItem = combobox.appendItem(NirdizatiUtil.localizeText(it.getCaption()))
                comboItem.setValue(it)
            }

            combobox.isReadonly = true
            combobox.setConstraint("no empty")
            combobox.selectedItem = combobox.items.first()
            (combobox.selectedItem.getValue() as Chart).render()

            combobox.addEventListener(Events.ON_SELECT, { e -> (((e as SelectEvent<*, *>).selectedItems.first() as Comboitem).getValue() as Chart).render() })
            hlayout.appendChild(combobox)

            val row = Row()
            row.align = "center"
            row.valign = "center"

            val cell = populateRow(row)
            cell.appendChild(hlayout)
            cell.sclass = "selected-option"
            selectionRows.appendChild(row)
        }
    }



    private fun generatePropertyRow() {
        val row = Row()
        row.align = "center"
        generatePropertyData(row)
        propertyRows.appendChild(row)
    }

    private fun generateMetadataRow() {
        val row = Row()
        row.align = "center"
        generateMainData(row)
        metadataRows.appendChild(row)
    }

    private fun generatePropertyData(row: Row) {
        job!!.learner.properties.forEach { generateLabelAndValue(row, "property." + it.id, it.property, false) }
    }

    private fun generateMainData(row: Row) {
        var param = job!!.encoding
        generateLabelAndValue(row, param.type, param.type + "." + param.id)
        param = job!!.bucketing
        generateLabelAndValue(row, param.type, param.type + "." + param.id)
        param = job!!.learner
        generateLabelAndValue(row, param.type, param.type + "." + param.id)
    }

    private fun generateLabelAndValue(row: Row, labelCaption: String, valueCaption: String, localizeValue: Boolean = true) {
        val label = Label(Labels.getLabel(labelCaption) + ": ")
        val value = Label(if (localizeValue) Labels.getLabel(valueCaption) else valueCaption)
        value.style = "font-weight: bold"

        val hlayout = Hlayout()
        hlayout.appendChild(label)
        hlayout.appendChild(value)
        row.appendChild(hlayout)
    }
}