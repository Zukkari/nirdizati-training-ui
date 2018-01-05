package cs.ut.business.engine

import cs.ut.business.jobs.Job
import cs.ut.business.jobs.SimulationJob
import org.apache.log4j.Logger
import java.util.concurrent.CopyOnWriteArrayList


object JobManager {
    val log: Logger = Logger.getLogger(JobManager::class.java)!!

    private val executedJobs: MutableMap<String, MutableList<Job>> = mutableMapOf()

    private val subscribers: CopyOnWriteArrayList<Notifiable> = CopyOnWriteArrayList()

    @Synchronized
    fun subscribeForUpdates(caller: Notifiable) {
        log.debug("New subscriber for updates -> ${caller::class.java}")
        subscribers.add(caller)
    }

    @Synchronized
    fun unsubscribeFromUpdates(caller: Notifiable) {
        log.debug("Removing subscriber ${caller::class.java}")
        subscribers.remove(caller)
    }

    internal fun statusUpdated(job: Job) {
        log.debug("Update event: ${job.id} -> notifying subscribers")
        subscribers.forEach {
            it.onUpdate(executedJobs.entries.firstOrNull { it.value.contains(job) }?.key ?: "", job)
        }
    }

    private fun jobsDeployed(key: String, jobs: List<Job>) {
        subscribers.forEach {
            it.onDeploy(key, jobs)
        }
    }

    @Synchronized
    fun deployJobs(key: String, jobs: List<Job>) {
        log.debug("Jobs to be executed for client $key -> $jobs")

        val deployed: MutableList<Job> = executedJobs[key]?.toMutableList() ?: mutableListOf()
        log.debug("Client $key has ${deployed.size} completed jobs")
        log.debug("Deploying ${jobs.size} jobs")

        jobs.forEach {
            NirdizatiThreadPool.execute(it)
            deployed.add(it)
        }

        log.debug("Updating completed job status for $key")
        executedJobs[key] = deployed
        log.debug("Successfully updated $key -> $deployed")


        log.debug("Successfully deployed all jobs to worker")
        jobsDeployed(key, jobs)
    }

    fun runServiceJob(job: Job) {
        NirdizatiThreadPool.execute(job)
    }

    fun getJobsForKey(key: String) = executedJobs[key]

    @Synchronized
    fun removeJob(key: String, simulationJob: SimulationJob) {
        log.debug("Removing job $simulationJob for client $key")
        executedJobs[key]?.remove(simulationJob)
    }
}