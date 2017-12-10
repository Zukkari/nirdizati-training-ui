package cs.ut.engine

import cs.ut.config.items.ModelParameter
import cs.ut.controllers.JobTrackerController
import cs.ut.controllers.MainPageController
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.ui.NirdizatiGrid
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Session
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Button
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class JobManager {
    companion object Manager {
        val log = Logger.getLogger(JobManager::class.java)!!

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
            val desktop = Executions.getCurrent().desktop
            desktop.enableServerPush(true)

            val jobs = jobQueue[currentSession] ?: LinkedList()

            encodings.forEach { encoding ->
                bucketing.forEach { bucketing ->
                    learner.forEach { learner ->
                        val job = SimulationJob(encoding, bucketing, learner, result, logFile!!, desktop)
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
            val executor = NirdizatiThreadPool()

            val currentJobs = jobQueue[Executions.getCurrent().session]!!
            log.debug("Deploying ${currentJobs.size} jobs")

            val grid = Executions.getCurrent().desktop.components.first { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
            grid.generate(currentJobs.toList().reversed(), false)
            grid.isVisible = true

            while (currentJobs.peek() != null) {
                executor.execute(currentJobs.poll())
            }

            log.debug("Successfully deployed all jobs to worker")
        }

        fun flushJobs() {
            jobQueue[Executions.getCurrent().session]?.clear()
            log.debug("Cleared all jobs for session ${Executions.getCurrent().session}")
        }

        fun notifyOfJobStatusChange(job: Job) {
            val grid: NirdizatiGrid<Job> = job.client.components.first { it.id == JobTrackerController.GRID_ID } as NirdizatiGrid<Job>
            updateJobStatus(job, grid.rows.getChildren(), grid)
        }

        tailrec private fun updateJobStatus(job: Job, rows: List<Row>, grid: NirdizatiGrid<Job>) {
            if (rows.isNotEmpty()) {
                val row = rows.first()
                val buttons = row.lastChild.lastChild.getChildren<Component>()
                val statusLabel = row.firstChild.getChildren<Component>()[1].lastChild.firstChild as Label

                if (job == row.getValue()) {
                    Executions.schedule(job.client,
                            { _ ->
                                statusLabel.value = job.status.name
                                buttons.forEach { (it as Button).isDisabled = job.status != JobStatus.COMPLETED }
                            },
                            Event("job_status", null, "update"))
                } else {
                    updateJobStatus(job, rows.drop(1), grid)
                }
            }
        }
    }
}