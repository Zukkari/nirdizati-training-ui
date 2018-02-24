package cs.ut.ui.adapters

import cs.ut.engine.item.Property
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.adapters.JobValueAdapter.Companion.jobArg
import cs.ut.ui.controllers.validation.ValidationController
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_VALIDATION
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.A
import org.zkoss.zul.Html
import org.zkoss.zul.Label
import org.zkoss.zul.Popup
import org.zkoss.zul.Row

class ValidationViewAdapter(private val parentController: ValidationController?, private val container: Component?) :
    GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()


    override fun provide(data: Job): Row {
        return provide(data, true)
    }

    fun provide(data: Job, addRedirectListener: Boolean = true): Row {
        data as SimulationJob
        return Row().also {
            it.align = "center"
            it.appendChild(Label(data.logFile.nameWithoutExtension))
            it.appendChild(getLabel(data.outcome.toString()))
            it.appendChild(getLabel(data.bucketing.toString()))
            it.appendChild(getLabel(data.encoding.toString()))
            it.appendChild(getLabel(data.learner.toString()))
            it.appendChild(A().apply { loadTooltip(this, data) })

            if (addRedirectListener) {
                it.addEventListener(Events.ON_CLICK, { _ ->
                    Executions.getCurrent().setAttribute(jobArg, data)
                    this.parentController!!.setContent(PAGE_VALIDATION, parentController.page())
                })
            }
        }
    }

    fun loadTooltip(a: A, data: SimulationJob) {
        a.iconSclass = "z-icon-question-circle"
        a.sclass = "validation-btn"
        a.vflex = "1"
        a.addEventListener(Events.ON_MOUSE_OVER, { _ ->
            a.desktop.components.firstOrNull { it.id == PROP_POPUP }?.detach()
            Popup().also {
                it.appendChild(Html(data.formTooltip()))
                it.id = PROP_POPUP
                container?.appendChild(it)
            }.open(a, "after_end ")
        })
        a.addEventListener(Events.ON_MOUSE_OUT, { _ ->
            a.desktop.components.filter { it is Popup }.forEach { (it as Popup).close() }
        })
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