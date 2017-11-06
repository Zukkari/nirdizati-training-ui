package cs.ut.engine

import cs.ut.config.items.ModelParameter
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Session
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class JobManager {
    companion object Manager {
        val log = Logger.getLogger(JobManager::class.java)!!

        private val completedJobs: List<Job> = arrayListOf()
        private val jobQueue: MutableMap<Session, Queue<Job>> = HashMap()

        var logFile: File? = null

        fun generateJobs(parameters: Map<String, List<ModelParameter>>) {
            logFile ?: throw NirdizatiRuntimeException("Log file is null")

            log.debug("Started generating jobs...")
            val start = System.currentTimeMillis()

            val encodings = parameters["encoding"]!!
            val bucketing = parameters["bucketing"]!!
            val learner = parameters["learner"]!!
            val result = parameters["predictiontype"]!![0]

            val currentSession = Executions.getCurrent().session
            Executions.getCurrent().desktop.enableServerPush(true)

            val jobs = jobQueue[currentSession] ?: LinkedList()

            encodings.forEach { encoding ->
                bucketing.forEach { bucketing ->
                    learner.forEach { learner ->
                        val job = SimulationJob(encoding, bucketing, learner, result, logFile!!, Executions.getCurrent().desktop)
                        log.debug("Scheduled job $job")
                        jobs.add(job)
                    }
                }
            }

            jobQueue[currentSession] = jobs
            logFile = null

            val end = System.currentTimeMillis()
            log.debug("Finished generating ${jobs.size} jobs in <${end - start} ms>")
        }

        fun deployJobs() {
            val worker = Worker.getInstance()

            val currentJobs = jobQueue[Executions.getCurrent().session]!!
            log.debug("Deploying ${currentJobs.size} jobs")

            while (currentJobs.peek() != null) {
                worker.scheduleJob(currentJobs.poll())
            }

            log.debug("Successfully deployed all jobs to worker")
        }

        fun flushJobs() {
            jobQueue[Executions.getCurrent().session]?.clear()
            log.debug("Cleared all jobs for session ${Executions.getCurrent().session}")
        }

        fun getCurrentFile(): File {
            log.debug("Getting current log file for current session")

            val jobs = jobQueue[Executions.getCurrent().session]

            jobs?.let { return (jobs.peek() as SimulationJob).logFile }

            throw NirdizatiRuntimeException("Current execution has no jobs scheduled")
        }

        fun getPredictionType(): ModelParameter {
            log.debug("Getting current prediction type for current session")

            val jobs = jobQueue[Executions.getCurrent().session]
            jobs?.let { return (jobs.peek() as SimulationJob).outcome }

            throw NirdizatiRuntimeException("Current execution has no jobs scheduled")
        }
    }
}