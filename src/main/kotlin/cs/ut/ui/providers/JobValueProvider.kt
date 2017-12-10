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

        val label = formJobLabel(data, row)

        row.appendChild(label)
        row.setValue(data)

        fields.add(FieldComponent(label, status))

        return row
    }

    private fun formJobLabel(job: Job, row: Row): Vlayout {
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

        val fileLayout = generateFileInfo(job)
        fileLayout.hflex = "1"
        val fileContainer = Hlayout()
        fileContainer.appendChild(fileLayout)

        val btnContainer = Hbox()
        btnContainer.hflex = "1"
        btnContainer.pack = "end"
        btnContainer.appendChild(generateRemoveBtn(job, row))
        fileContainer.appendChild(btnContainer)

        val vlayout = Vlayout()
        vlayout.appendChild(fileContainer)

        vlayout.appendChild(generateStatus(label, job))
        vlayout.appendChild(bottom)

        val hlayout = Hlayout()
        hlayout.appendChild(getVisualizeBtn(job))
        hlayout.appendChild(getDeployBtn())
        vlayout.appendChild(hlayout)

        return vlayout
    }

    private fun generateStatus(label: Label, job: Job): Hlayout {
        val labelStatusContainer = Hlayout()
        val labelContainer = Hlayout()
        labelContainer.appendChild(label)
        label.hflex = "1"
        labelStatusContainer.appendChild(labelContainer)

        val status = Label(job.status.name)
        val statusContainer = Hbox()
        statusContainer.appendChild(status)
        statusContainer.hflex = "1"
        statusContainer.vflex = "1"
        statusContainer.pack = "end"
        statusContainer.align = "center"
        labelStatusContainer.appendChild(statusContainer)

        return labelStatusContainer
    }

    private fun generateFileInfo(job: SimulationJob): Hlayout {
        val fileLabel = Label(NirdizatiUtil.localizeText("attribute.log_file"))
        fileLabel.style = "font-weight: bold;"

        val file = Label(job.logFile.name)

        val fileLayout = Hlayout()
        fileLayout.appendChild(fileLabel)
        fileLayout.appendChild(file)
        return fileLayout
    }

    private fun generateRemoveBtn(job: Job, row: Row): Button {
        val btn = Button("x")
        btn.vflex = "min"
        btn.hflex = "min"
        btn.sclass = "close-btn"

        btn.addEventListener(Events.ON_CLICK, { _ ->
            val grid: NirdizatiGrid<Job> = job.client.components.first { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
            (job as SimulationJob).kill()
            Executions.schedule(job.client,
                    { _ ->
                        grid.removeRow(row)
                        if (!grid.isVisible) {
                            job.client.components.first { it.id == TRACKER_EAST }.isVisible = false
                        }
                    },
                    Event("abort_job", null, null))
        })

        return btn
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