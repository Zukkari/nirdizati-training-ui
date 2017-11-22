package cs.ut.ui.providers

import cs.ut.controllers.JobTrackerController
import cs.ut.jobs.Job
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Window

class JobValueProvider : GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: Job): Row {
        val label = Label(data.toString())
        val status = Label(data.status.name)

        val row = Row()
        row.appendChild(label)
        row.appendChild(status)
        row.setValue(data)
        row.sclass = JobTrackerController.GRID_ID

        row.addEventListener(Events.ON_CLICK, { _ ->
            val args = mapOf<String, Any>("data" to data)
            val window: Window = Executions.createComponents(
                    "/views/modals/job_info.zul",
                    data.client.components.first { it.id == "contentInclude" },
                    args
            ) as Window
            window.doModal()
        })

        fields.add(FieldComponent(label, status))

        return row
    }
}