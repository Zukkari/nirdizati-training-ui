package cs.ut.jobs

import cs.ut.engine.IdProvider
import cs.ut.engine.JobManager
import org.apache.log4j.Logger

enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}

abstract class Job : Runnable {
    val log = Logger.getLogger(Job::class.java)!!

    val id: String = IdProvider.getNextId()

    var status: JobStatus = JobStatus.PENDING
    protected var stop = false

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
                log.debug("Job $id has been stopped by the user")
                return
            }

            updateEvent()
            preProcess()
        } catch (e: Exception) {
            log.debug("Job $id failed in preprocess stage", e)
            status = JobStatus.FAILED
            updateEvent()
            return
        }

        log.debug("Job $id finished preprocess step")

        try {
            log.debug("Job $id started execute stage")
            status = JobStatus.RUNNING

            if (stop) {
                log.debug("Job $id has been stopped by the user")
                return
            }

            updateEvent()
            execute()
        } catch (e: Exception) {
            log.debug("Job $id failed in execute stage", e)
            status = JobStatus.FAILED
            updateEvent()
            return
        }

        log.debug("Job $id finished execute step")

        try {
            log.debug("Job $id started post execute step")
            status = JobStatus.FINISHING

            if (stop) {
                log.debug("Job $id has been stopped by the user")
                return
            }

            updateEvent()
            postExecute()
        } catch (e: Exception) {
            log.debug("Job $id failed in post execute step")
            status = JobStatus.FAILED
            return
        }

        log.debug("Job $id completed successfully")
        status = JobStatus.COMPLETED
        updateEvent()

        if (stop) {
            log.debug("Job $this has been stopped by the user")
            return
        }
    }

    private fun updateEvent() {
        JobManager.statusUpdated(this)
    }

    open fun kill() {
        stop = true
    }


}