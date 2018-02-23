package cs.ut.ui.controllers.validation

import cs.ut.charts.Chart
import cs.ut.charts.ChartGenerator
import cs.ut.charts.MAE
import cs.ut.jobs.JobService
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.ui.adapters.ComparisonAdapter
import cs.ut.ui.adapters.JobValueAdapter
import cs.ut.ui.adapters.ValidationViewAdapter
import cs.ut.ui.controllers.Redirectable
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_MODELS_OVERVIEW
import cs.ut.util.PAGE_TRAINING
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
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Combobox
import org.zkoss.zul.Comboitem
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Rows
import org.zkoss.zul.Vbox

class SingleJobValidationController : SelectorComposer<Component>(), Redirectable {
    private val log = NirdizatiLogger.getLogger(SingleJobValidationController::class.java)
    private lateinit var job: SimulationJob
    private lateinit var charts: Map<String, List<Chart>>

    @Wire
    private lateinit var mainContainer: Vbox

    @Wire
    private lateinit var propertyRows: Rows

    @Wire
    private lateinit var selectionRows: Rows

    @Wire
    private lateinit var comboLayout: Vbox

    @Wire
    private lateinit var comparisonContainer: Vbox

    private var currentlySelected: String = ""

    var accuracyMode: String = ""

    @Wire
    private lateinit var compRows: Rows

    val checkBoxes = mutableListOf<Checkbox>()

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        job = Executions.getCurrent().getAttribute(JobValueAdapter.jobArg) as SimulationJob
        log.debug("Received job argument $job, initializing in read only mode")
        charts = ChartGenerator(job).getCharts().groupBy { it.javaClass.name }

        val provider = ComparisonAdapter(mainContainer, this)
        (listOf(job) +
                JobService.findSimilarJobs(CookieUtil.getCookieKey(Executions.getCurrent().nativeRequest), job))
            .map { provider.provide(it) }.forEach { compRows.appendChild(it) }

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
        propertyRows.appendChild(ValidationViewAdapter(null, mainContainer).provide(job, false))
        generateChartOptions()
    }

    private fun generateChartOptions() {
        val row = Row()
        row.align = "center"
        charts.forEach { row.generateCell(it) }
        selectionRows.appendChild(row)
        Events.postEvent("onClick", row.getChildren<Component>().first { it.id == ACCURACY_COMPARISON }, null)
    }

    private fun Row.generateCell(entry: Map.Entry<String, List<Chart>>) {
        val cell = Cell()
        val label = Label(NirdizatiUtil.localizeText(entry.key))
        cell.id = entry.key
        cell.align = "center"
        cell.valign = "center"

        cell.addEventListener(Events.ON_CLICK, { _ ->
            selectionRows.getChildren<Row>().first().getChildren<Cell>().forEach { it.sclass = "val-cell" }
            cell.sclass = "val-cell selected-option"
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
            comboLayout.parent.parent.isVisible = false
            this.render()
            setVisibility()
        }
    }

    private fun setVisibility() {
        comparisonContainer.parent.parent.isVisible = currentlySelected == ACCURACY_COMPARISON
        if (currentlySelected == ACCURACY_COMPARISON) {
            addDataSets()
        }
    }

    private fun addDataSets() {
        checkBoxes.filter { it.isChecked && (it.getValue() as? SimulationJob)?.id != job.id }.forEach {
            val value = it.getValue<SimulationJob>() as SimulationJob
            ComparisonAdapter.addDataSet(value.id, ComparisonAdapter.getPayload(value, this))
        }
    }

    private fun removeChildren() {
        comboLayout.getChildren<Component>().clear()
    }

    private fun List<Chart>.generateListenerForMany(): SerializableEventListener<Event> {
        return SerializableEventListener { _ ->
            removeChildren()

            comboLayout.parent.parent.isVisible = true

            var itemSet = false
            val comboBox = Combobox()
            comboBox.hflex = "max"
            this.forEach {
                val comboItem = comboBox.appendItem(NirdizatiUtil.localizeText(it.getCaption()))
                comboItem.setValue(it)

                if (MAE == it.name) {
                    comboBox.selectedItem = comboItem
                    itemSet = true
                    accuracyMode = it.name
                }
            }

            if (!itemSet) comboBox.selectedItem = comboBox.items.first().apply {
                accuracyMode = (this.getValue() as Chart).name
            }

            comboBox.isReadonly = true
            comboBox.setConstraint("no empty")
            (comboBox.selectedItem.getValue() as Chart).render()

            comboBox.addEventListener(
                Events.ON_SELECT,
                { e ->
                    (((e as SelectEvent<*, *>).selectedItems.first() as Comboitem).getValue() as Chart).apply {
                        accuracyMode = this.name
                        this.render()
                        addDataSets()
                    }
                })
            comboLayout.appendChild(comboBox)
            setVisibility()
        }
    }

    companion object {
        private const val ACCURACY_COMPARISON = "cs.ut.charts.LineChart"
    }
}