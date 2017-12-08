package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.controllers.MainPageController
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.NirdizatiGrid
import cs.ut.util.PAGE_VALIDATION
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.*

class JobValueProvider(val parent: Hbox) : GridValueProvider<Job, Row> {
    companion object {
        const val jobArg = "JOB"

        fun generateButtons(job: SimulationJob): Vlayout {
            val vlayout = Vlayout()

            val visualize = Button(Labels.getLabel("job_tracker.visiualize"))
            visualize.hflex = "1"
            visualize.isDisabled = !(JobStatus.COMPLETED == job.status || JobStatus.FINISHING == job.status)

            visualize.addEventListener(Events.ON_CLICK, { _ ->
                Executions.getCurrent().setAttribute(jobArg, job)
                MainPageController.getInstance().setContent(PAGE_VALIDATION, Executions.getCurrent().desktop.firstPage)
            })

            val deploy = Button(Labels.getLabel("job_tracker.deploy_to_runtime"))
            deploy.isDisabled = true
            deploy.hflex = "1"

            val btnRow = Row()
            btnRow.appendChild(visualize)
            btnRow.appendChild(deploy)

            vlayout.appendChild(visualize)
            vlayout.appendChild(deploy)

            return vlayout
        }
    }

    override var fields: MutableList<FieldComponent> = mutableListOf()
    lateinit var originator: NirdizatiGrid<Job>

    override fun provide(data: Job): Row {
        val status = Label(data.status.name)

        val row = Row()
        val label = formJobLabel(data)

        row.appendChild(label)
        row.appendChild(status)
        row.setValue(data)

        fields.add(FieldComponent(label, status))

        return row
    }

    private fun formJobLabel(job: Job): Vlayout {
        job as SimulationJob

        val encoding = job.encoding
        val bucketing = job.bucketing
        val learner = job.learner

        val label = Label(Labels.getLabel(encoding.type + "." + encoding.id) + "\n" +
                Labels.getLabel(bucketing.type + "." + bucketing.id) + "\n" +
                Labels.getLabel(learner.type + "." + learner.id))
        label.isPre = true
        label.style = "font-weight: bold;"

        val bottom = formHyperparamRow(learner)

        val vlayout = Vlayout()
        vlayout.appendChild(label)
        vlayout.appendChild(bottom)
        vlayout.appendChild(generateButtons(job))

        return vlayout
    }

    private fun formHyperparamRow(learner: ModelParameter): Label {
        var label = ""
        val iterator = learner.properties.iterator()

        while (iterator.hasNext()) {
            val prop = iterator.next()
            label += Labels.getLabel("property." + prop.id) + ": " + prop.property + "\n"
        }

        val res = Label(label)
        res.style = "font-size: 10px"
        res.isMultiline = true
        res.isPre = true
        return res
    }
}