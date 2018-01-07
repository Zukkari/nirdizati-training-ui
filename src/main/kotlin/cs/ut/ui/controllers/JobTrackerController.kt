package cs.ut.ui.controllers

import cs.ut.engine.JobManager
import cs.ut.engine.Notifiable
import cs.ut.engine.events.DeployEvent
import cs.ut.engine.events.NirdizatiEvent
import cs.ut.engine.events.StatusUpdateEvent
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.ui.NirdizatiGrid
import cs.ut.ui.adapters.JobValueAdataper
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
import cs.ut.util.TRACKER_EAST
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Button
import org.zkoss.zul.Hbox
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import javax.servlet.http.HttpServletRequest

class JobTrackerController : SelectorComposer<Component>(), Redirectable, Notifiable {
    @Wire
    private lateinit var jobTracker: Hbox

    companion object {
        const val GRID_ID = "tracker_grid"
        const val TRACKER = "tracker"
    }

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        self.desktop.enableServerPush(true)
        JobManager.subscribeForUpdates(this)

        val jobGrid = NirdizatiGrid(JobValueAdataper())
        jobGrid.vflex = "1"
        jobGrid.id = GRID_ID
        jobTracker.appendChild(jobGrid)
    }

    override fun onUpdate(event: NirdizatiEvent) {
        when (event) {
            is StatusUpdateEvent -> event.updateJobStatus()
            is DeployEvent -> event.updateDeployment()
        }
    }

    private fun StatusUpdateEvent.updateJobStatus() {
        Executions.schedule(self.desktop,
                { _ ->
                    val subKey: String = CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
                    if (subKey == this.target) {
                        val grid = Executions.getCurrent().desktop.components.first { it.id == GRID_ID } as NirdizatiGrid<Job>
                        this.data.updateJobStatus(grid.rows.getChildren())
                    }
                },
                Event("job_update"))
    }

    private fun DeployEvent.updateDeployment() {
        Executions.schedule(self.desktop,
                { _ ->
                    val subKey: String = CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
                    if (subKey == this.target) {

                        val comps = Executions.getCurrent().desktop.components
                        val tracker = comps.first { it.id == TRACKER_EAST }
                        tracker.isVisible = true

                        val grid = comps.first { it.id == GRID_ID } as NirdizatiGrid<Job>
                        grid.generate(this.data, false)
                    }
                },
                Event("deployment"))
    }

    override fun isAlive(): Boolean {
        return self.desktop != null && self.desktop.isAlive
    }

    tailrec private fun Job.updateJobStatus(rows: List<Row>) {
        if (rows.isNotEmpty()) {
            val row = rows.first()
            val buttons = row.lastChild.lastChild.getChildren<Component>()
            val statusLabel = row.firstChild.getChildren<Component>()[1].lastChild.firstChild as Label

            if (this == row.getValue()) {
                statusLabel.value = this.status.name
                buttons.forEach { (it as Button).isDisabled = this.status != JobStatus.COMPLETED }

                if (this.status == JobStatus.COMPLETED) {
                    NirdizatiUtil.showNotificationAsync(
                            NirdizatiUtil.localizeText("job.completed.simulation", this),
                            Executions.getCurrent().desktop)
                }
            } else {
                updateJobStatus(rows.drop(1))
            }
        }
    }
}