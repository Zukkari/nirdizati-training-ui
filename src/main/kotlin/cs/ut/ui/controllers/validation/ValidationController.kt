package cs.ut.ui.controllers.validation

import cs.ut.engine.JobManager
import cs.ut.jobs.Job
import cs.ut.ui.NirdizatiGrid
import cs.ut.ui.adapters.ValidationViewAdapter
import cs.ut.ui.controllers.Redirectable
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
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
    private lateinit var mainContainer: Vbox

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        val userJobs =
            JobManager.loadJobsFromStorage(CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest))

        if (userJobs.isEmpty()) {
            emptyLayout()
            return
        }

        NirdizatiGrid(ValidationViewAdapter()).apply {
            this.configure()
            this.generate(userJobs)
            mainContainer.appendChild(this)
        }
    }

    private fun emptyLayout() {
        mainContainer.appendChild(Vbox().apply {
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
    }

    private fun NirdizatiGrid<Job>.configure() {
        this.setColumns(
            mapOf(
                "encoding" to "",
                "bucketing" to "",
                "learner" to "",
                "predictiontype" to "",
                "logfile" to "",
                "hyperparameters" to "min"
            )
        )
        this.hflex = "1"
        this.vflex = "1"
        this.columns.getChildren<Column>().forEach { it.align = "center" }
    }
}