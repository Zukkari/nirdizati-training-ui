package cs.ut.ui.adapters

import cs.ut.config.items.ModelParameter
import cs.ut.controllers.JobTrackerController
import cs.ut.controllers.Redirectable
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.NirdizatiGrid
import cs.ut.util.NirdizatiUtil
import cs.ut.util.OUTCOME
import cs.ut.util.PAGE_VALIDATION
import cs.ut.util.TRACKER_EAST
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.*

class JobValueAdataper : GridValueProvider<Job, Row>, Redirectable {
    companion object {
        const val jobArg = "JOB"
    }

    override var fields: MutableList<FieldComponent> = mutableListOf()

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

    private fun Job.identifierLabel(): Label {
        val label = Label(this.id)
        label.style = "font-size: 8px"
        return label
    }

    private fun ModelParameter.generateResultLabel(): Hlayout {
        val hlayout = Hlayout()

        val label = Label(NirdizatiUtil.localizeText("property.outcome"))
        label.style = "font-weight: bold;"

        val outcome = Label(NirdizatiUtil.localizeText(if (this.translate) this.getTranslateName() else this.id))
        hlayout.appendChild(label)
        hlayout.appendChild(outcome)

        return hlayout
    }

    private fun Row.formJobLabel(job: Job): Vlayout {
        job as SimulationJob

        val encoding = job.encoding
        val bucketing = job.bucketing
        val learner = job.learner
        val outcome = job.outcome

        val label = Label(NirdizatiUtil.localizeText(encoding.type + "." + encoding.id) + "\n" +
                NirdizatiUtil.localizeText(bucketing.type + "." + bucketing.id) + "\n" +
                NirdizatiUtil.localizeText(learner.type + "." + learner.id)
        )
        label.isPre = true
        label.style = "font-weight: bold;"

        val outcomeText = "" + if (outcome.id == OUTCOME) NirdizatiUtil.localizeText("threshold.threshold_msg") + ": " +
                (if (outcome.parameter == "-1") NirdizatiUtil.localizeText("threshold.avg").toLowerCase()
                else outcome.parameter) + "\n"
        else ""

        val bottom: Label = learner.formHyperparamRow()
        bottom.value = outcomeText + bottom.value

        val fileLayout = job.generateFileInfo()
        fileLayout.hflex = "1"

        val labelsContainer = Vlayout()
        labelsContainer.appendChild(fileLayout)
        labelsContainer.appendChild(job.outcome.generateResultLabel())
        labelsContainer.hflex = "1"

        val fileContainer = Hlayout()
        fileContainer.appendChild(labelsContainer)

        val btnContainer = Hbox()
        btnContainer.hflex = "min"
        btnContainer.pack = "end"
        btnContainer.appendChild(job.generateRemoveBtn(this))
        fileContainer.appendChild(btnContainer)
        fileContainer.hflex = "1"

        val vlayout = Vlayout()
        vlayout.appendChild(fileContainer)

        vlayout.appendChild(job.generateStatus(label))
        vlayout.appendChild(bottom)
        vlayout.appendChild(job.identifierLabel())

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
                        if (grid.rows.getChildren<Component>().isEmpty()) {
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
            setContent(PAGE_VALIDATION, Executions.getCurrent().desktop.firstPage)
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