package cs.ut.ui.adapters

import cs.ut.config.items.Property
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
import org.zkoss.zk.ui.event.OpenEvent
import org.zkoss.zul.A
import org.zkoss.zul.Html
import org.zkoss.zul.Label
import org.zkoss.zul.Popup
import org.zkoss.zul.Row

class ValidationViewAdapter(private val parentController: ValidationController) : GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()


    override fun provide(data: Job): Row {
        data as SimulationJob
        return Row().also {
            it.align = "center"
            it.appendChild(Label(data.logFile.nameWithoutExtension))
            it.appendChild(getLabel(data.outcome.toString()))
            it.appendChild(getLabel(data.bucketing.toString()))
            it.appendChild(getLabel(data.encoding.toString()))
            it.appendChild(getLabel(data.learner.toString()))
            it.appendChild(A().apply {
                this.iconSclass = "z-icon-question-circle"
                this.sclass = "validation-btn"
                this.vflex = "1"
                this.addEventListener(Events.ON_MOUSE_OVER, { _ ->
                    Popup().also {
                        it.appendChild(Html(data.formTooltip()))
                        it.id = PROP_POPUP

                        val comp = desktop.components.firstOrNull { it.id == PROP_POPUP } as Popup?
                        if (comp == null) {
                            parentController.mainContainer.appendChild(it)
                        } else {
                            comp.open(this, "after_end")
                        }
                    }.open(this, "after_end ")
                })
                this.addEventListener(Events.ON_MOUSE_OUT, { _ ->
                    desktop.components.filter { it is Popup }.forEach { (it as Popup).close() }
                })
            })

            it.addEventListener(Events.ON_CLICK, { _ ->
                Executions.getCurrent().setAttribute(jobArg, data)
                this.parentController.setContent(PAGE_VALIDATION, parentController.page())
            })
        }
    }

    private fun SimulationJob.formTooltip(): String {
        val parameters = mutableListOf<Property>().also {
            it.addAll(this.encoding.properties)
            it.addAll(this.bucketing.properties)
            it.addAll(this.learner.properties)
        }

        return parameters.joinToString(
            separator = "<br/>",
            transform = { "<b>" + NirdizatiUtil.localizeText("property.${it.id}") + "</b>: ${it.property}" })
    }

    private fun getLabel(str: String) = Label(NirdizatiUtil.localizeText(str))

    companion object {
        const val PROP_POPUP = "propertyPopUpMenu"
    }
}