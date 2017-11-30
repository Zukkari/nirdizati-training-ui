package cs.ut.jobs

import cs.ut.config.MasterConfiguration
import cs.ut.controllers.MainPageController
import cs.ut.engine.JobManager
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import java.util.*
import kotlin.NoSuchElementException

abstract class Job(val client: Desktop) : Runnable {
    val log = Logger.getLogger(Job::class.java)!!

    var createTime: Date = Date()
    abstract var startTime: Date
    abstract var completeTime: Date
    var status: JobStatus = JobStatus.PENDING

    val pathProvider = MasterConfiguration.getInstance().directoryPathConfiguration
    protected val scriptDir = pathProvider.scriptDirectory
    protected val userModelDir = pathProvider.userModelDirectory
    protected val coreDir = scriptDir + "core/"
    protected val datasetDir = pathProvider.datasetDirectory
    protected val trainingDir = pathProvider.trainDirectory
    protected val pklDir = pathProvider.pklDirectory

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
            JobManager.notifyOfJobStatusChange(this)
            preProcess()
        } catch (e: Exception) {
            log.debug("Job $this failed in preprocess stage", e)
            status = JobStatus.FAILED
            JobManager.notifyOfJobStatusChange(this)
            return
        }

        log.debug("Job $this finished preprocess step")

        try {
            log.debug("Job $this started execute stage")
            status = JobStatus.RUNNING
            JobManager.notifyOfJobStatusChange(this)
            execute()
        } catch (e: Exception) {
            log.debug("Job $this failed in execute stage", e)
            status = JobStatus.FAILED
            JobManager.notifyOfJobStatusChange(this)
            return
        }

        log.debug("Job $this finished execute step")

        try {
            log.debug("Job $this started post execute step")
            status = JobStatus.FINISHING
            JobManager.notifyOfJobStatusChange(this)
            postExecute()
        } catch (e: Exception) {
            log.debug("Job $this failed in post execute step")
            status = JobStatus.FAILED

            try {
                JobManager.notifyOfJobStatusChange(this)
            } catch (e: NoSuchElementException) {
                log.debug("Could not notify $client of job status change: $e")
            }
            return
        }

        log.debug("Job $this completed successfully")
        completeTime = Calendar.getInstance().time
        status = JobStatus.COMPLETED

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
}


enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}