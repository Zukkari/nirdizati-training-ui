package cs.ut.engine

import cs.ut.jobs.JobStatus
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.util.BUCKETING
import cs.ut.util.ENCODING
import cs.ut.util.LEARNER
import cs.ut.util.PREDICTIONTYPE
import cs.ut.util.readTrainingJson
import java.io.File

data class CacheItem<T>(private val items: MutableList<T> = mutableListOf()) {
    fun addItem(item: T) = items.add(item)

    fun addItems(items: List<T>) = this.items.addAll(items)

    fun rawData() = items
}

abstract class CacheHolder<T> {
    protected val cachedItems = mutableMapOf<String, CacheItem<T>>()

    open fun addToCache(key: String, item: T) = (cachedItems[key] ?: createNewItem(key)).addItem(item)

    open fun retrieveFromCache(key: String): CacheItem<T> = cachedItems[key] ?: CacheItem()

    private fun createNewItem(key: String): CacheItem<T> = CacheItem<T>().apply { cachedItems[key] = this }

    fun flush() = cachedItems.clear()
}

class JobCacheHolder : CacheHolder<SimulationJob>() {
    val logger = NirdizatiLogger.getLogger(this::class.java)

    override fun retrieveFromCache(key: String): CacheItem<SimulationJob> {
        val existing: CacheItem<SimulationJob>? = cachedItems[key]

        return when (existing) {
            is CacheItem<SimulationJob> -> existing
            else -> fetchFromDisk(key)
        }
    }

    private fun fetchFromDisk(key: String): CacheItem<SimulationJob> =
        CacheItem<SimulationJob>().apply {
            val items = loadFromDisk(key)
            this.addItems(items)
        }


    private fun loadFromDisk(key: String): List<SimulationJob> {
        return mutableListOf<SimulationJob>().also { c ->
            LogManager.loadJobIds(key)
                .filter { it.id !in JobManager.queue.map { it.id }}
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

