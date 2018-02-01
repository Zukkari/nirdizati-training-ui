package cs.ut.jobs


import cs.ut.engine.IdProvider
import cs.ut.engine.JobManager
import cs.ut.logging.NirdLogger


enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}

abstract class Job(generatedId: String = "") : Runnable {
    val log = NirdLogger(caller = this.javaClass)

    val id: String = if (generatedId.isBlank()) IdProvider.getNextId() else generatedId

    var status: JobStatus = JobStatus.PENDING

    open fun preProcess() {}

    open fun execute() {}

    open fun postExecute() {}

    open fun isNotificationRequired() = false

    open fun getNotificationMessage() = ""

    open fun beforeInterrupt() = Unit

    override fun run() {
        log.debug("Started job execution: $this")

        try {
            log.debug("Stared preprocess stage")
            status = JobStatus.PREPARING

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
    }

    private fun updateEvent() {
        JobManager.statusUpdated(this)
    }
}