package cs.ut.ui.controllers

import cs.ut.business.engine.JobManager
import cs.ut.business.engine.Notifiable
import cs.ut.business.jobs.Job
import cs.ut.business.jobs.JobStatus
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

    override fun onUpdate(key: String, job: Job) {
        if (self.desktop == null || !self.desktop.isAlive) {
            JobManager.unsubscribeFromUpdates(this)
        } else {
            Executions.schedule(self.desktop,
                    { _ ->
                        val subKey: String = CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
                        if (subKey == key) {
                            val grid = Executions.getCurrent().desktop.components.first { it.id == GRID_ID } as NirdizatiGrid<Job>
                            job.updateJobStatus(grid.rows.getChildren())
                        }
                    },
                    Event("job_update"))
        }
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

    override fun onDeploy(key: String, jobs: List<Job>) {
        if (self.desktop == null || !self.desktop.isAlive) {
            JobManager.unsubscribeFromUpdates(this)
        } else {
            Executions.schedule(self.desktop,
                    { _ ->
                        val subKey: String = CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
                        if (subKey == key) {

                            val comps = Executions.getCurrent().desktop.components
                            val tracker = comps.first { it.id == TRACKER_EAST }
                            tracker.isVisible = true

                            val grid = comps.first { it.id == GRID_ID } as NirdizatiGrid<Job>
                            grid.generate(jobs, false)
                        }
                    },
                    Event("deployment"))
        }
    }
}