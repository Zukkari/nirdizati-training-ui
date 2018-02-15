package cs.ut.engine

import cs.ut.engine.events.DeployEvent
import cs.ut.engine.events.NirdizatiEvent
import cs.ut.engine.events.StatusUpdateEvent
import cs.ut.engine.events.findCallback
import cs.ut.jobs.Job
import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger
import java.lang.ref.WeakReference
import java.util.concurrent.Future


object JobManager {
    val log = NirdizatiLogger.getLogger(JobManager::class.java)

    val cache: CacheHolder<SimulationJob> = Cache.jobCache
    private var subscribers: List<WeakReference<Any>> = listOf()
    private val jobStatus: MutableMap<Job, Future<*>> = mutableMapOf()

    val queue: MutableList<SimulationJob> = mutableListOf()

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

        if (job.status == JobStatus.COMPLETED && job in queue && job is SimulationJob) {
            queue.remove(job)
            cache.addToCache(job.owner, job)
        }

        handleEvent(StatusUpdateEvent(job))
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
        log.debug("Deploying ${jobs.size} jobs")

        synchronized(jobStatus) {
            jobs.forEach {
                jobStatus[it] = NirdizatiThreadPool.execute(it)
                queue.add(it as SimulationJob)
            }
        }

        log.debug("Updating completed job status for $key")

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

    fun getJobsForKey(key: String): List<SimulationJob> {
        val cached: List<SimulationJob> = cache.retrieveFromCache(key).rawData()
        val pending: List<SimulationJob> = queue.filter { it.owner == key }
        return (pending.toList() + cached.toList())
    }

    fun getCompletedJobs(key: String): List<SimulationJob> =
        getJobsForKey(key).filter { it.status == JobStatus.COMPLETED }
}