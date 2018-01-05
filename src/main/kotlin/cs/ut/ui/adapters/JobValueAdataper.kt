package cs.ut.ui.adapters

import cs.ut.business.engine.JobManager
import cs.ut.business.jobs.Job
import cs.ut.business.jobs.JobStatus
import cs.ut.business.jobs.SimulationJob
import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.NirdizatiGrid
import cs.ut.ui.controllers.JobTrackerController
import cs.ut.ui.controllers.Redirectable
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
import cs.ut.util.OUTCOME
import cs.ut.util.PAGE_VALIDATION
import cs.ut.util.TRACKER_EAST
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Button
import org.zkoss.zul.Hbox
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Vlayout
import javax.servlet.http.HttpServletRequest

class JobValueAdataper : GridValueProvider<Job, Row>, Redirectable {
    companion object {
        const val jobArg = "JOB"
        val AVERAGE = MasterConfiguration.defaultValuesConfiguration.average.toString()
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
        label.sclass = "extra-small"
        return label
    }

    private fun ModelParameter.generateResultLabel(): Hlayout {
        val hlayout = Hlayout()

        val label = Label(NirdizatiUtil.localizeText("property.outcome"))
        label.sclass = "param-label"

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
        label.sclass = "param-label"

        val outcomeText = "" + if (outcome.id == OUTCOME) NirdizatiUtil.localizeText("threshold.threshold_msg") + ": " +
                (if (outcome.parameter == AVERAGE) NirdizatiUtil.localizeText("threshold.avg").toLowerCase()
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
        fileLabel.sclass = "param-label"

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

        val client = Executions.getCurrent().desktop
        btn.addEventListener(Events.ON_CLICK, { _ ->
            val grid: NirdizatiGrid<Job> = client.components.firstOrNull { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
            this.kill()
            Executions.schedule(client,
                    { _ ->
                        row.detach()
                        if (grid.rows.getChildren<Component>().isEmpty()) {
                            client.components.first { it.id == TRACKER_EAST }.isVisible = false
                        }
                    },
                    Event("abort_job", null, null))
            JobManager.removeJob(
                    CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest),
                    this)
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
        res.sclass = "small-font"
        res.isMultiline = true
        res.isPre = true
        return res
    }
}