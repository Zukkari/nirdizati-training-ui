package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.controllers.MainPageController
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.NirdizatiGrid
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_VALIDATION
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.*

class JobValueProvider(val parent: Hbox) : GridValueProvider<Job, Row> {
    companion object {
        const val jobArg = "JOB"
    }

    override var fields: MutableList<FieldComponent> = mutableListOf()
    lateinit var originator: NirdizatiGrid<Job>

    override fun provide(data: Job): Row {
        val status = Label(data.status.name)

        val row = Row()
        row.valign = "end"

        val label = formJobLabel(data)

        row.appendChild(label)
        row.setValue(data)

        fields.add(FieldComponent(label, status))

        return row
    }

    private fun formJobLabel(job: Job): Vlayout {
        job as SimulationJob

        val encoding = job.encoding
        val bucketing = job.bucketing
        val learner = job.learner

        val label = Label(NirdizatiUtil.localizeText(encoding.type + "." + encoding.id) + "\n" +
                NirdizatiUtil.localizeText(bucketing.type + "." + bucketing.id) + "\n" +
                NirdizatiUtil.localizeText(learner.type + "." + learner.id))
        label.isPre = true
        label.style = "font-weight: bold;"

        val bottom = formHyperparamRow(learner)

        val vlayout = Vlayout()
        vlayout.appendChild(label)
        vlayout.appendChild(bottom)
        vlayout.appendChild(formJobMetadata(job))

        val hlayout = Hlayout()
        hlayout.appendChild(getVisualizeBtn(job))
        hlayout.appendChild(getDeployBtn())
        vlayout.appendChild(hlayout)

        return vlayout
    }

    private fun getVisualizeBtn(job: SimulationJob): Button {
        val visualize = Button(NirdizatiUtil.localizeText("job_tracker.visiualize"))
        visualize.hflex = "1"
        visualize.isDisabled = !(JobStatus.COMPLETED == job.status || JobStatus.FINISHING == job.status)

        visualize.addEventListener(Events.ON_CLICK, { _ ->
            Executions.getCurrent().setAttribute(jobArg, job)
            MainPageController.getInstance().setContent(PAGE_VALIDATION, Executions.getCurrent().desktop.firstPage)
        })

        return visualize
    }

    private fun getDeployBtn(): Button {
        val deploy = Button(NirdizatiUtil.localizeText("job_tracker.deploy_to_runtime"))
        deploy.isDisabled = true
        deploy.hflex = "1"
        deploy.vflex = "min"

        return deploy
    }

    private fun formJobMetadata(job: SimulationJob): Vlayout {
        val statusLabel = Label(NirdizatiUtil.localizeText("attribute.job_status"))
        statusLabel.style = "font-weight: bold;"

        val status = Label(job.status.name)

        val statusLayout = Hlayout()
        statusLayout.appendChild(statusLabel)
        statusLayout.appendChild(status)

        val fileLabel = Label(NirdizatiUtil.localizeText("attribute.log_file"))
        fileLabel.style = "font-weight: bold;"

        val file = Label(job.logFile.name)

        val fileLayout = Hlayout()
        fileLayout.appendChild(fileLabel)
        fileLayout.appendChild(file)

        val res = Vlayout()
        res.appendChild(fileLayout)
        res.appendChild(statusLayout)

        return res
    }

    private fun formHyperparamRow(learner: ModelParameter): Label {
        var label = ""
        val iterator = learner.properties.iterator()

        while (iterator.hasNext()) {
            val prop = iterator.next()
            label += NirdizatiUtil.localizeText("property." + prop.id) + ": " + prop.property + "\n"
        }

        val res = Label(label)
        res.style = "font-size: 10px"
        res.isMultiline = true
        res.isPre = true
        return res
    }
}