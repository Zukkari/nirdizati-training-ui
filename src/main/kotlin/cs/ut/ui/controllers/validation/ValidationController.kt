package cs.ut.ui.controllers.validation

import cs.ut.engine.JobManager
import cs.ut.engine.events.Callback
import cs.ut.engine.events.StatusUpdateEvent
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.NirdizatiGrid
import cs.ut.ui.adapters.ValidationViewAdapter
import cs.ut.ui.controllers.Redirectable
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Button
import org.zkoss.zul.Column
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Vbox
import javax.servlet.http.HttpServletRequest

class ValidationController : SelectorComposer<Component>(), Redirectable {

    @Wire
    lateinit var mainContainer: Vbox

    @Wire
    private lateinit var grid: NirdizatiGrid<Job>

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)
        generate()
        JobManager.subscribe(this)
    }

    private fun generate() {
        val userJobs =
            JobManager
                .cache
                .retrieveFromCache((CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)))
                .rawData()

        grid = NirdizatiGrid(ValidationViewAdapter(this)).apply {
            this.configure()
        }

        if (userJobs.isEmpty()) {
            emptyLayout()
            return
        }

        grid.generate(userJobs)
        mainContainer.appendChild(grid)
    }

    fun page() = this.page ?: throw NirdizatiRuntimeException("No current page set")

    private fun emptyLayout() {
        mainContainer.appendChild(Vbox().apply {
            this.align = "center"
            this.pack = "center"
            this.appendChild(
                Label(NirdizatiUtil.localizeText("validation.empty1")).apply {
                    this.sclass = "large-text"
                })
            this.appendChild(
                Label(NirdizatiUtil.localizeText("validation.empty2")).apply {
                    this.sclass = "large-text"
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
    }

    private fun NirdizatiGrid<Job>.configure() {
        this.setColumns(
            mapOf(
                "logfile" to "",
                "predictiontype" to "",
                "bucketing" to "",
                "encoding" to "",
                "learner" to "",
                "hyperparameters" to "min"
            )
        )
        this.hflex = "1"
        this.vflex = "1"
        this.columns.getChildren<Column>().forEach { it.align = "center" }
    }

    @Callback(StatusUpdateEvent::class)
    fun updateContent(event: StatusUpdateEvent) {
        if (self.desktop == null || !self.desktop.isAlive) {
            return
        }

        when (event.data) {
            is SimulationJob -> {
                if (event.data.status == JobStatus.COMPLETED) {
                    Executions.schedule(
                        self.desktop, { _ ->
                            val userJobs =
                                JobManager
                                    .cache
                                    .retrieveFromCache(CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest))
                                    .rawData()
                                    .reversed()
                            if (grid.parent != mainContainer) {
                                mainContainer.getChildren<Component>().clear()
                                mainContainer.appendChild(grid)
                            }

                            grid.generate(userJobs, true)
                        },
                        Event("content_update")
                    )
                }
            }
        }
    }
}