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

/**
 * Used to generate metadata info about the job in validation views
 */
class ValidationViewAdapter(private val parentController: ValidationController?, private val container: Component?) :
    GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()


    override fun provide(data: Job): Row {
        return provide(data, true)
    }

    /**
     * Overloaded method that says whether redirection listener should be added to given row
     * @param data to generate row from
     * @param addRedirectListener should redirect listener be added
     *
     * @return row with generated data
     */
    fun provide(data: Job, addRedirectListener: Boolean = true): Row {
        data as SimulationJob
        return Row().also {
            it.sclass = if (addRedirectListener) "pointer" else "no-hover-effect"
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

    /**
     * Load tooltip for given job and attach to given element
     *
     * @param a to attach to
     * @param data to generate tooltip from
     */
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

    /**
     * Create tooltip that contains info about hyper parameters for the job
     */
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

    /**
     * Helper to create label with content
     * @param str to be localized and appended to label
     *
     * @return label with localized text
     */
    private fun getLabel(str: String) = Label(NirdizatiUtil.localizeText(str))

    companion object {
        const val PROP_POPUP = "propertyPopUpMenu"
    }
}