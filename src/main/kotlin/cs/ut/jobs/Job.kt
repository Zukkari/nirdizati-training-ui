package cs.ut.jobs


import cs.ut.engine.IdProvider
import cs.ut.engine.JobManager
import cs.ut.logging.NirdizatiLogger
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}

open class Job protected constructor(generatedId: String = "") : Runnable {
    val log = NirdizatiLogger.getLogger(Job::class.java)

    val id: String = if (generatedId.isBlank()) IdProvider.getNextId() else generatedId

    var status: JobStatus = JobStatus.PENDING

    lateinit var startTime: String

    open fun preProcess() = Unit

    open fun execute() = Unit

    open fun postExecute() = Unit

    open fun isNotificationRequired() = false

    open fun getNotificationMessage() = ""

    open fun beforeInterrupt() = Unit

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

    private fun updateEvent() {
        JobManager.statusUpdated(this)
    }

    private fun start(): String {
        val date = Date()
        val df = DateTimeFormatter.ISO_INSTANT
        return df.format(date.toInstant())
    }
}