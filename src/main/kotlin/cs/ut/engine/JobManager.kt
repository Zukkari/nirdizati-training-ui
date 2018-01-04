package cs.ut.engine

import cs.ut.config.items.ModelParameter
import cs.ut.controllers.JobTrackerController
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.ui.NirdizatiGrid
import cs.ut.util.CookieUtil
import cs.ut.util.TRACKER_EAST
import cs.ut.util.isColumnStatic
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import java.io.File
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.collections.HashMap

object JobManager {
    val log = Logger.getLogger(JobManager::class.java)!!

    private val jobQueue: MutableMap<String, Queue<Job>> = HashMap()
    private val executedJobs: MutableMap<String, MutableList<Job>> = mutableMapOf()

    private val cookieUtil = CookieUtil()

    var logFile: File? = null

    fun generateJobs(parameters: Map<String, List<ModelParameter>>) {
        logFile ?: throw NirdizatiRuntimeException("Log file is null")

        log.debug("Started generating jobs...")
        val start = System.currentTimeMillis()

        val encodings = parameters["encoding"]!!
        val bucketing = parameters["bucketing"]!!
        val learner = parameters["learner"]!!
        val result = parameters["predictiontype"]!![0]

        val key = cookieUtil.getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
        val desktop = Executions.getCurrent().desktop

        val jobs = jobQueue[key] ?: LinkedList()

        encodings.forEach { encoding ->
            bucketing.forEach { bucketing ->
                learner.forEach { learner ->
                    val job = SimulationJob(
                            encoding,
                            bucketing,
                            learner,
                            result,
                            isColumnStatic(result.parameter, FilenameUtils.getBaseName((logFile as File).name)),
                            logFile!!,
                            desktop)

                    log.debug("Scheduled job $job")
                    jobs.add(job)
                }
            }
        }

        jobQueue[key] = jobs
        logFile = null

        val end = System.currentTimeMillis()
        log.debug("Finished generating ${jobs.size} jobs in <${end - start} ms>")
    }

    fun deployJobs() {
        val key: String = cookieUtil.getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
        val currentJobs = jobQueue[key]!!
        log.debug("Jobs to be executed for client $key -> $currentJobs")

        val completed: MutableList<Job> = executedJobs[key]?.toMutableList() ?: mutableListOf()
        log.debug("Client $key has ${completed.size} completed jobs")
        log.debug("Deploying ${currentJobs.size} jobs")

        val grid = Executions.getCurrent().desktop.components.first { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
        grid.generate(currentJobs.toList().reversed(), false)
        grid.isVisible = true
        Executions.getCurrent().desktop.components.first { it.id == TRACKER_EAST }.isVisible = true

        while (currentJobs.peek() != null) {
            val job: Job = currentJobs.poll()
            NirdizatiThreadPool.execute(job)
            completed.add(job)
        }

        log.debug("Updating completed job status for $key")
        executedJobs[key] = completed
        log.debug("Successfully updated $key -> $completed")

        log.debug("Successfully deployed all jobs to worker")
    }

    fun flushJobs() {
        jobQueue[cookieUtil.getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)]?.clear()
        log.debug("Cleared all jobs for session ${Executions.getCurrent().session}")
    }

    fun getJobsForKey(key: String) = executedJobs[key]


    fun updateJobs(key: String, desktop: Desktop) {
        val jobs: List<Job> = executedJobs[key]!!
        jobs.forEach { it.client = desktop }
    }

    fun removeJob(simulationJob: SimulationJob) {
        val cookieKey: String = cookieUtil.getCookieKey(simulationJob.client.execution.nativeRequest as HttpServletRequest)
        log.debug("Removing job $simulationJob for client $cookieKey")
        executedJobs[cookieKey]?.remove(simulationJob)
    }
}