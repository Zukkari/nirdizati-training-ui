package cs.ut.controllers

import cs.ut.jobs.SimulationJob
import cs.ut.ui.providers.JobValueProvider
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Rows

class ValidationController : SelectorComposer<Component>() {
    private val log = Logger.getLogger(ValidationController::class.java)
    private var job: SimulationJob? = null

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