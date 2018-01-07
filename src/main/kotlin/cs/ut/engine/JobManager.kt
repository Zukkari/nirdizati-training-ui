package cs.ut.engine

import cs.ut.engine.events.DeployEvent
import cs.ut.engine.events.NirdizatiEvent
import cs.ut.engine.events.StatusUpdateEvent
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import org.apache.log4j.Logger


object JobManager {
    val log: Logger = Logger.getLogger(JobManager::class.java)!!

    private val executedJobs: MutableMap<String, MutableList<Job>> = mutableMapOf()
    private val subscribers: MutableList<Notifiable> = mutableListOf()

    fun subscribeForUpdates(caller: Notifiable) {
        synchronized(subscribers) {
            log.debug("New subscriber for updates -> ${caller::class.java}")
            subscribers.add(caller)
        }
    }

    fun unsubscribeFromUpdates(caller: Notifiable) {
        synchronized(subscribers) {
            log.debug("Removing subscriber ${caller::class.java}")
            subscribers.remove(caller)
        }
    }

    internal fun statusUpdated(job: Job) {
        log.debug("Update event: ${job.id} -> notifying subscribers")
        handleEvent(StatusUpdateEvent(executedJobs.entries.firstOrNull { it.value.contains(job) }?.key ?: "", job))
    }

    private fun handleEvent(event: NirdizatiEvent) {
        val deadSubs = mutableListOf<Notifiable>()
        synchronized(subscribers) {
            subscribers.forEach {
                if (it.isAlive()) {
                    it.onUpdate(event)
                } else {
                    deadSubs.add(it)
                }
            }
        }
        deadSubs.cleanSubscribers()
    }

    private fun List<Notifiable>.cleanSubscribers() {
        log.debug("Received ${this.size} to remove from subscribers")
        this.forEach {
            unsubscribeFromUpdates(it)
        }
    }

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
        handleEvent(DeployEvent(key, jobs))
    }

    fun runServiceJob(job: Job) {
        log.debug("Running service job $job")
        NirdizatiThreadPool.execute(job)
    }

    fun getJobsForKey(key: String) = executedJobs[key]

    fun removeJob(key: String, simulationJob: SimulationJob) {
        synchronized(executedJobs) {
            log.debug("Removing job $simulationJob for client $key")
            executedJobs[key]?.remove(simulationJob)
        }
    }
}