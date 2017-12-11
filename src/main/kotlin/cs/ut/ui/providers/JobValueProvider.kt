package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.controllers.JobTrackerController
import cs.ut.controllers.MainPageController
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.NirdizatiGrid
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_VALIDATION
import cs.ut.util.TRACKER_EAST
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Button
import org.zkoss.zul.Hbox
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Vlayout

class JobValueProvider() : GridValueProvider<Job, Row> {
    companion object {
        const val jobArg = "JOB"
    }

    override var fields: MutableList<FieldComponent> = mutableListOf()
    lateinit var originator: NirdizatiGrid<Job>

    override fun provide(data: Job): Row {
        val status = Label(data.status.name)

        val row = Row()
        row.valign = "end"

        val label = row.formJobLabel(data)

        row.appendChild(label)
        row.setValue(data)

        fields.add(FieldComponent(label, status))

        return row
    }

    private fun Row.formJobLabel(job: Job): Vlayout {
        job as SimulationJob

        val encoding = job.encoding
        val bucketing = job.bucketing
        val learner = job.learner
        val outcome = job.outcome

        val label = Label(NirdizatiUtil.localizeText(encoding.type + "." + encoding.id) + "\n" +
                NirdizatiUtil.localizeText(bucketing.type + "." + bucketing.id) + "\n" +
                NirdizatiUtil.localizeText(learner.type + "." + learner.id) + "\n" +
                NirdizatiUtil.localizeText(outcome.type + "." + outcome.id))
        label.isPre = true
        label.style = "font-weight: bold;"

        val bottom = learner.formHyperparamRow()

        val fileLayout = job.generateFileInfo()
        fileLayout.hflex = "1"
        val fileContainer = Hlayout()
        fileContainer.appendChild(fileLayout)

        val btnContainer = Hbox()
        btnContainer.hflex = "1"
        btnContainer.pack = "end"
        btnContainer.appendChild(job.generateRemoveBtn(this))
        fileContainer.appendChild(btnContainer)

        val vlayout = Vlayout()
        vlayout.appendChild(fileContainer)

        vlayout.appendChild(job.generateStatus(label))
        vlayout.appendChild(bottom)

        val hlayout = Hlayout()
        hlayout.appendChild(job.getVisualizeBtn())
        hlayout.appendChild(getDeployBtn())
        vlayout.appendChild(hlayout)

        return vlayout
    }

    private fun SimulationJob.generateStatus(label: Label): Hlayout {
        val labelStatusContainer = Hlayout()
        val labelContainer = Hlayout()
        labelContainer.appendChild(label)
        label.hflex = "1"
        labelStatusContainer.appendChild(labelContainer)

        val status = Label(this.status.name)
        val statusContainer = Hbox()
        statusContainer.appendChild(status)
        statusContainer.hflex = "1"
        statusContainer.vflex = "1"
        statusContainer.pack = "end"
        statusContainer.align = "center"
        labelStatusContainer.appendChild(statusContainer)

        return labelStatusContainer
    }

    private fun SimulationJob.generateFileInfo(): Hlayout {
        val fileLabel = Label(NirdizatiUtil.localizeText("attribute.log_file"))
        fileLabel.style = "font-weight: bold;"

        val file = Label(this.logFile.name)

        val fileLayout = Hlayout()
        fileLayout.appendChild(fileLabel)
        fileLayout.appendChild(file)
        return fileLayout
    }

    private fun SimulationJob.generateRemoveBtn(row: Row): Button {
        val btn = Button("x")
        btn.vflex = "min"
        btn.hflex = "min"
        btn.sclass = "close-btn"

        btn.addEventListener(Events.ON_CLICK, { _ ->
            val grid: NirdizatiGrid<Job> = this.client.components.first { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
            this.kill()
            Executions.schedule(this.client,
                    { _ ->
                        row.detach()
                        if (!grid.isVisible) {
                            this.client.components.first { it.id == TRACKER_EAST }.isVisible = false
                        }
                    },
                    Event("abort_job", null, null))
        })

        return btn
    }

    private fun SimulationJob.getVisualizeBtn(): Button {
        val visualize = Button(NirdizatiUtil.localizeText("job_tracker.visiualize"))
        visualize.hflex = "1"
        visualize.isDisabled = !(JobStatus.COMPLETED == this.status || JobStatus.FINISHING == this.status)

        visualize.addEventListener(Events.ON_CLICK, { _ ->
            Executions.getCurrent().setAttribute(jobArg, this)
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

    private fun ModelParameter.formHyperparamRow(): Label {
        var label = ""
        val iterator = this.properties.iterator()

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