package cs.ut.jobs

import cs.ut.config.MasterConfiguration
import cs.ut.controllers.JobTrackerController
import cs.ut.controllers.MainPageController
import cs.ut.ui.NirdizatiGrid
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Button
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import java.util.*

abstract class Job(val client: Desktop) : Runnable {
    val log = Logger.getLogger(Job::class.java)!!

    var createTime: Date = Date()
    abstract var startTime: Date
    abstract var completeTime: Date
    var status: JobStatus = JobStatus.PENDING
    protected var stop = false

    val pathProvider = MasterConfiguration.getInstance().directoryPathConfiguration!!
    protected val scriptDir = pathProvider.scriptDirectory!!
    protected val userModelDir = pathProvider.userModelDirectory!!
    protected val coreDir = scriptDir + "core/"
    protected val datasetDir = pathProvider.datasetDirectory!!
    protected val trainingDir = pathProvider.trainDirectory!!
    protected val pklDir = pathProvider.pklDirectory!!

    open fun preProcess() {}

    open fun execute() {}

    open fun postExecute() {}

    open fun isNotificationRequired() = false

    open fun getNotificationMessage() = ""

    override fun run() {
        log.debug("Started job execution: $this")

        try {
            log.debug("Stared preprocess stage")
            status = JobStatus.PREPARING

            if (stop) {
                log.debug("Job $this has been stopped by the user")
                return
            }

            notifyOfJobStatusChange()
            preProcess()
        } catch (e: Exception) {
            log.debug("Job $this failed in preprocess stage", e)
            status = JobStatus.FAILED
            notifyOfJobStatusChange()
            return
        }

        log.debug("Job $this finished preprocess step")

        try {
            log.debug("Job $this started execute stage")
            status = JobStatus.RUNNING

            if (stop) {
                log.debug("Job $this has been stopped by the user")
                return
            }

            notifyOfJobStatusChange()
            execute()
        } catch (e: Exception) {
            log.debug("Job $this failed in execute stage", e)
            status = JobStatus.FAILED
            notifyOfJobStatusChange()
            return
        }

        log.debug("Job $this finished execute step")

        try {
            log.debug("Job $this started post execute step")
            status = JobStatus.FINISHING

            if (stop) {
                log.debug("Job $this has been stopped by the user")
                return
            }

            notifyOfJobStatusChange()
            postExecute()
        } catch (e: Exception) {
            log.debug("Job $this failed in post execute step")
            status = JobStatus.FAILED

            try {
                notifyOfJobStatusChange()
            } catch (e: NoSuchElementException) {
                log.debug("Could not notify $client of job status change: $e")
            }
            return
        }

        log.debug("Job $this completed successfully")
        completeTime = Calendar.getInstance().time
        status = JobStatus.COMPLETED

        if (stop) {
            log.debug("Job $this has been stopped by the user")
            return
        }

        if (isNotificationRequired()) {
            Executions.schedule(client,
                    { _ ->
                        Clients.showNotification(
                                getNotificationMessage(),
                                "info",
                                MainPageController.getInstance().comp,
                                "bottom_center",
                                -1)
                    },
                    Event("jobStatus", null, "complete"))
        }
    }

    open fun kill() {
        stop = true
    }

    private fun notifyOfJobStatusChange() {
        val grid: NirdizatiGrid<Job> = this.client.components.first { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
        this.updateJobStatus(grid.rows.getChildren(), grid)
    }

    tailrec private fun updateJobStatus(rows: List<Row>, grid: NirdizatiGrid<Job>) {
        if (rows.isNotEmpty()) {
            val row = rows.first()
            val buttons = row.lastChild.lastChild.getChildren<Component>()
            val statusLabel = row.firstChild.getChildren<Component>()[1].lastChild.firstChild as Label

            if (this == row.getValue()) {
                Executions.schedule(this.client,
                        { _ ->
                            statusLabel.value = this.status.name
                            buttons.forEach { (it as Button).isDisabled = this.status != JobStatus.COMPLETED }
                        },
                        Event("job_status", null, "update"))
            } else {
                updateJobStatus(rows.drop(1), grid)
            }
        }
    }
}


enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}