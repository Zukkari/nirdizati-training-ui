package cs.ut.ui.controllers

import cs.ut.charts.Chart
import cs.ut.charts.ChartGenerator
import cs.ut.charts.MAE
import cs.ut.engine.JobManager
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdLogger
import cs.ut.ui.adapters.JobValueAdataper
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
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
import javax.servlet.http.HttpServletRequest

class ValidationController : SelectorComposer<Component>(), Redirectable {
    private val log = NirdLogger(NirdLogger.getId(Executions.getCurrent().nativeRequest), this.javaClass)
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
    private lateinit var logSelect: Hbox

    @Wire
    private lateinit var canvas: Include

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
        logSelect.isVisible = true
        initLayout()
    }

    private fun initLayout() {
        val jobs =
            JobManager.loadJobsFromStorage(CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest))

        if (jobs.isNotEmpty()) {
            addLayoutContent(jobs)
            log.debug("Generated layout for no context mode")
            handleSelection(jobs.first())
        } else {
            logSelect.vflex = "1"
            logSelect.appendChild(
                Vbox().apply {
                    this.align = "center"
                    this.pack = "center"
                    this.appendChild(
                        Label(NirdizatiUtil.localizeText("validation.empty1")).apply {
                            this.sclass = "no-logs-found"
                        })
                    this.appendChild(
                        Label(NirdizatiUtil.localizeText("validation.empty2")).apply {
                            this.sclass = "no-logs-found"
                        })
                    this.appendChild(
                        Hlayout().apply {
                            this.vflex = "min"
                            this.hflex = "min"
                            this.sclass = "margin-top-7px"
                            this.appendChild(
                                Button(NirdizatiUtil.localizeText("validation.train")).also {
                                    it.addEventListener(Events.ON_CLICK, { _ ->
                                        this@ValidationController.setContent(cs.ut.util.PAGE_TRAINING, page)
                                    })
                                    it.sclass = "n-btn"
                                }
                            )
                        }

                    )
                }
            )
            logSelect.parent.apply {
                this.getChildren<Component>().clear()
                this.appendChild(logSelect)
            }
        }
    }

    private fun clearLayouts() {
        metadataRows.getChildren<Component>().clear()
        propertyRows.getChildren<Component>().clear()
        selectionRows.getChildren<Component>().clear()
        canvas.src = null
        canvas.src = "/views/graphs/graph_canvas.html"
    }

    private fun addLayoutContent(jobs: List<SimulationJob>) {
        val combo = Combobox().apply {
            this.addEventListener(Events.ON_SELECT, { e ->
                e as SelectEvent<*, *>
                log.debug("Generating layout for new job -> ${e.data}")
                clearLayouts()
                handleSelection((e.selectedItems.first() as Comboitem).getValue())
            })

            jobs.forEach {
                val comboItem = this.appendItem(it.id)
                comboItem.setValue(it)
            }

            this.selectedItem = this.items[0]
            this.sclass = "id-combo"
            this.width = "340px"
            this.isReadonly = true
        }
        logSelect.appendChild(Label(NirdizatiUtil.localizeText("validation.select_completed")).apply {
            this.sclass = "param-label"
            this.vflex = "min"
        })
        logSelect.appendChild(combo)
    }

    private fun handleSelection(job: SimulationJob) {
        this.job = job
        charts = ChartGenerator(job).getCharts().groupBy { it.javaClass.name }
        generateReadOnlyMode()
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