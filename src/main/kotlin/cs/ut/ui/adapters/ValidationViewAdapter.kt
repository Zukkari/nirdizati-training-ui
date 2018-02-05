package cs.ut.ui.adapters

import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.adapters.JobValueAdataper.Companion.jobArg
import cs.ut.ui.controllers.validation.ValidationController
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_VALIDATION
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Button
import org.zkoss.zul.Label
import org.zkoss.zul.Row

class ValidationViewAdapter(private val parent: ValidationController) : GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()


    override fun provide(data: Job): Row {
        data as SimulationJob
        return Row().also {
            it.align = "center"
            it.appendChild(getLabel(data.encoding.toString()))
            it.appendChild(getLabel(data.bucketing.toString()))
            it.appendChild(getLabel(data.learner.toString()))
            it.appendChild(getLabel(data.outcome.toString()))
            it.appendChild(Label(data.logFile.nameWithoutExtension))
            it.appendChild(Button().apply {
                this.iconSclass = "z-icon-question-circle"
                this.sclass = "validation-btn"
                this.hflex = "min"
                this.vflex = "min"
            })

            it.addEventListener(Events.ON_CLICK, { _ ->
                Executions.getCurrent().setAttribute(jobArg, data)
                this.parent.setContent(PAGE_VALIDATION, parent.page())
            })
        }
    }

    private fun getLabel(str: String) = Label(NirdizatiUtil.localizeText(str))
}