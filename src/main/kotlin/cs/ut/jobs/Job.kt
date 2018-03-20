package cs.ut.jobs


import cs.ut.engine.IdProvider
import cs.ut.engine.JobManager
import cs.ut.logging.NirdizatiLogger
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Enum that represents job status
 */
enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}

/**
 * Abstract class that represents job structure
 */
abstract class Job protected constructor(generatedId: String = "") : Runnable {
    val log = NirdizatiLogger.getLogger(Job::class.java)

    val id: String = if (generatedId.isBlank()) IdProvider.getNextId() else generatedId

    var status: JobStatus = JobStatus.PENDING

    lateinit var startTime: String

    /**
     * Action to be performed before job execution
     */
    open fun preProcess() = Unit

    /**
     * Action to be performed in execution stage
     */
    open fun execute() = Unit

    /**
     * Action to be performed after execute stage
     */
    open fun postExecute() = Unit

    /**
     * Should user be notified of job completion
     */
    open fun isNotificationRequired() = false

    /**
     * Notification message to show to the user
     */
    open fun getNotificationMessage() = ""

    /**
     * Function that is called before interrupting the thread on graceful shutdown
     */
    open fun beforeInterrupt() = Unit

    /**
     * Running the job
     */
    override fun run() {
        log.debug("Started job execution: $this")
        startTime = start()

        try {
            log.debug("Stared pre process stage")
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

    /**
     * Status have been updated, notify job manager
     */
    private fun updateEvent() {
        JobManager.statusUpdated(this)
    }

    /**
     * Get start time for the job
     *
     * @return start time in ISO format as string
     */
    private fun start(): String {
        val date = Date()
        val df = DateTimeFormatter.ISO_INSTANT
        return df.format(date.toInstant())
    }
}