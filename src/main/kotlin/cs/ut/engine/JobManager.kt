package cs.ut.engine

import cs.ut.engine.events.DeployEvent
import cs.ut.engine.events.NirdizatiEvent
import cs.ut.engine.events.StatusUpdateEvent
import cs.ut.engine.events.findCallback
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.util.BUCKETING
import cs.ut.util.ENCODING
import cs.ut.util.LEARNER
import cs.ut.util.PREDICTIONTYPE
import cs.ut.util.readTrainingJson
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.Future


object JobManager {
    val log= NirdizatiLogger.getLogger(JobManager::class.java)

    private val executedJobs: MutableMap<String, MutableList<Job>> = mutableMapOf()
    private var subscribers: List<WeakReference<Any>> = listOf()
    private val jobStatus: MutableMap<Job, Future<*>> = mutableMapOf()

    fun subscribe(caller: Any) {
        synchronized(subscribers) {
            log.debug("New subscriber for updates -> ${caller::class.java}")
            subscribers += WeakReference(caller)
        }
    }

    fun unsubscribe(caller: Any) {
        synchronized(subscribers) {
            log.debug("Removing subscriber ${caller::class.java}")
            subscribers.firstOrNull { it.get() == caller }?.let { subscribers -= it }
        }
    }

    fun statusUpdated(job: Job) {
        log.debug("Update event: ${job.id} -> notifying subscribers")
        handleEvent(StatusUpdateEvent(executedJobs.entries.firstOrNull { it.value.contains(job) }?.key ?: "", job))
    }

    private fun handleEvent(event: NirdizatiEvent) {
        cleanSubscribers()
        synchronized(subscribers) {
            subscribers.forEach {
                notify(it, event)
            }
        }
    }

    private fun cleanSubscribers() {
        val before: Int = subscribers.size
        subscribers = subscribers.filter { it.get() != null }
        val after: Int = subscribers.size
        if (before > after) {
            log.debug("Unsubscribed ${before - after} callbacks")
        }
    }

    private fun notify(ref: WeakReference<Any>, event: NirdizatiEvent) {
        val obj = ref.get() ?: return
        findCallback(obj::class.java, event::class)?.invoke(obj, event)
    }

    fun deployJobs(key: String, jobs: List<Job>) {
        log.debug("Jobs to be executed for client $key -> $jobs")

        val deployed: MutableList<Job> = executedJobs[key]?.toMutableList() ?: mutableListOf()
        log.debug("Client $key has ${deployed.size} completed jobs")
        log.debug("Deploying ${jobs.size} jobs")

        synchronized(jobStatus) {
            jobs.forEach {
                jobStatus[it] = NirdizatiThreadPool.execute(it)
                deployed.add(it)
            }
        }

        log.debug("Updating completed job status for $key")
        executedJobs[key] = deployed
        log.debug("Successfully updated $key -> $deployed")


        log.debug("Successfully deployed all jobs to worker")
        handleEvent(DeployEvent(key, jobs))
    }

    fun stopJob(job: Job) {
        log.debug("Stopping job ${job.id}")
        job.beforeInterrupt()
        log.debug("Completed before interrupt")
        jobStatus[job]?.cancel(true)
        log.debug("Job thread ${job.id} successfully interrupted")
    }

    fun runServiceJob(job: Job): Future<*> {
        log.debug("Running service job $job")
        return NirdizatiThreadPool.execute(job)
    }

    fun getJobsForKey(key: String) = executedJobs[key]

    fun removeJob(key: String, simulationJob: SimulationJob) {
        synchronized(executedJobs) {
            log.debug("Removing job $simulationJob for client $key")
            executedJobs[key]?.remove(simulationJob)
        }
    }

    fun loadJobsFromStorage(key: String): List<SimulationJob> {
        return mutableListOf<SimulationJob>().also { c ->
            LogManager.loadJobIds(key)
                .filter { data ->
                    val sessionJobs = executedJobs[key] ?: listOf<Job>()
                    data.id !in sessionJobs.map { it.id }
                            || sessionJobs.firstOrNull { it.id == data.id }?.status == JobStatus.COMPLETED
                }
                .forEach {
                    val params = readTrainingJson(it.id).flatMap { it.value }
                    c.add(SimulationJob(
                        params.first { it.type == ENCODING },
                        params.first { it.type == BUCKETING },
                        params.first { it.type == LEARNER },
                        params.first { it.type == PREDICTIONTYPE },
                        File(it.path),
                        key,
                        it.id
                    ).also { it.status = JobStatus.COMPLETED })
                }
        }
    }
}