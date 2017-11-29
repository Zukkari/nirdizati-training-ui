package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.NirdizatiGrid
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.*

class JobValueProvider(val parent: Hbox) : GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()
    lateinit var originator: NirdizatiGrid<Job>

    override fun provide(data: Job): Row {
        val status = Label(data.status.name)

        val row = Row()
        val label = formJobLabel(data)

        row.appendChild(label)
        row.appendChild(status)
        row.setValue(data)
        addRowListener(row)

        fields.add(FieldComponent(label, status))

        return row
    }

    private fun formJobLabel(job: Job): Vlayout {
        job as SimulationJob

        val encoding = job.encoding
        val bucketing = job.bucketing
        val learner = job.learner

        val label = Label(Labels.getLabel(encoding.type + "." + encoding.id) + " " +
                Labels.getLabel(bucketing.type + "." + bucketing.id) + " " +
                Labels.getLabel(learner.type + "." + learner.id))
        label.style = "font-weight: bold;"

        val bottom = formHyperparamRow(learner)

        val vlayout = Vlayout()
        vlayout.appendChild(label)
        vlayout.appendChild(bottom)

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
        res.style = "font-size: 8px"
        res.isMultiline = true
        res.isPre = true
        return res
    }

    private fun addRowListener(row: Row) {
        row.addEventListener(Events.ON_CLICK, { _ ->
            originator.isVisible = false
            val job = row.getValue<Job>() as SimulationJob
            val propertyList = generateParameters(job)

            val grid = NirdizatiGrid(AttributeToLabelsProvider())
            grid.generate(propertyList)
            parent.appendChild(grid)

            grid.addEventListener(Events.ON_CLICK, {_ ->
                parent.removeChild(grid)
                originator.isVisible = true
            })

            val visualize = Button(Labels.getLabel("job_tracker.visiualize"))
            visualize.isDisabled = true
            visualize.hflex = "1"

            val deploy = Button(Labels.getLabel("job_tracker.deploy_to_runtime"))
            deploy.isDisabled = true
            deploy.hflex = "1"

            val btnRow = Row()
            btnRow.appendChild(visualize)
            btnRow.appendChild(deploy)
            grid.rows.appendChild(btnRow)
        })
    }

    private fun generateParameters(job: SimulationJob): List<Any> {
        var propertyList = listOf(
                mapOf(Pair("log_file", job.logFile)),
                mapOf(Pair("create_time", job.createTime)),
                mapOf(Pair("start_time", if (job.status == JobStatus.PENDING) "" else job.startTime)),
                mapOf(Pair("complete_time", if (job.status == JobStatus.COMPLETED) job.completeTime else "")),
                job.status,
                job.encoding,
                job.bucketing,
                job.learner,
                job.outcome
        )
        propertyList += job.learner.properties
        return propertyList
    }
}