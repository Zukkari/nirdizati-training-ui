package cs.ut.ui.adapters

import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.util.NirdizatiUtil
import org.zkoss.zul.Button
import org.zkoss.zul.Label
import org.zkoss.zul.Row

class ValidationViewAdapter : GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()


    override fun provide(data: Job): Row {
        data as SimulationJob
        return Row().apply {
            this.align = "center"
            this.appendChild(getLabel(data.encoding.toString()))
            this.appendChild(getLabel(data.bucketing.toString()))
            this.appendChild(getLabel(data.learner.toString()))
            this.appendChild(getLabel(data.outcome.toString()))
            this.appendChild(Label(data.logFile.nameWithoutExtension))
            this.appendChild(Button().apply {
                this.iconSclass = "z-icon-question-circle"
                this.sclass = "validation-btn"
                this.hflex = "min"
                this.vflex = "min"
            })
        }
    }

    private fun getLabel(str: String) = Label(NirdizatiUtil.localizeText(str))
}