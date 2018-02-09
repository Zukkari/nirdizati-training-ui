package cs.ut.charts

import com.google.gson.Gson
import cs.ut.engine.Cache
import cs.ut.engine.CacheHolder
import cs.ut.engine.LogManager
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger

class ChartGenerator(val job: SimulationJob) {
    val log = NirdizatiLogger.getLogger(ChartGenerator::class.java)

    companion object {
        const val TRUE_VS_PREDICTED = "true_vs_predicted"
    }

    private val chartCache = Cache.chartCache[job.owner]

    private val gson = Gson()

    fun getCharts(): List<Chart> {
        log.debug("Fetching charts for job with id ${job.id} for client ${job.owner}")
        val cached = chartCache
        return when (cached) {
            is CacheHolder<Chart> -> getFromCache(cached)
            else -> {
                // No cache for this client, need to fetch and cache
                fetchCharts().apply {
                    synchronized(Cache.chartCache) {
                        Cache.chartCache[job.owner] = CacheHolder()
                        log.debug("Created new slot in cache for ${job.owner}")
                        Cache.chartCache[job.owner]!!.addToCache(job.id, this)
                        log.debug("Added ${this.size} items to cache")
                    }
                }
            }
        }
    }

    private fun getFromCache(cached: CacheHolder<Chart>): List<Chart> {
        log.debug("Fetching data for client exists")
        val charts = cached.retrieveFromCache(job.id)
        return if (charts.rawData().isEmpty()) {
            log.debug("Job ${job.id} is not cached for the client, fetching if from disk")
            // Fetch
            fetchCharts().apply {
                log.debug("Fetched ${this.size} items from disk")
                charts.addItems(this)
                log.debug("Added items to cache")
            }
        } else {
            // is cached
            log.debug("Charts are cached, returning cached version")
            charts.rawData()
        }
    }

    private fun fetchCharts(): MutableList<Chart> {
        val charts = mutableListOf<Chart>()

        if (LogManager.isClassification(job)) {
            charts.add(generateHeatMap())
            charts.addAll(generateLineCharts())
            charts.addAll(generateBarCharts())
        } else {
            charts.add(generateScatterPlot(TRUE_VS_PREDICTED))
            charts.addAll(generateLineCharts())
            charts.addAll(generateBarCharts())
        }

        return charts
    }

    private fun generateScatterPlot(name: String): ScatterPlot {
        val payload = getLinearPayload(LogManager.getDetailedFile(job), Mode.SCATTER)
        return ScatterPlot(name, gson.toJson(payload))
    }

    private fun generateLineCharts(): List<LineChart> {
        val payload = getLinearPayload(LogManager.getValidationFile(job), Mode.LINE).groupBy { it.dataType }
        var charts = listOf<LineChart>()
        payload.forEach { charts += LineChart(it.key, gson.toJson(it.value), it.value.last().x.toInt()) }
        return charts
    }

    private fun generateBarCharts(): List<BarChart> {
        val files = LogManager.getFeatureImportanceFiles(job)
        val charts = mutableListOf<BarChart>()

        (1..files.size).zip(files).forEach {
            val payload = getBarChartPayload(it.second)
            charts.add(
                BarChart(
                    it.first.toString(),
                    gson.toJson(payload.map { it.value }),
                    gson.toJson(payload.map { it.label })
                )
            )
        }

        return charts.toList()
    }

    private fun generateHeatMap(): HeatMap {
        val file = LogManager.getDetailedFile(job)
        val heatMap = getHeatMapPayload(file)

        return HeatMap(
            TRUE_VS_PREDICTED,
            gson.toJson(heatMap.data.map { arrayOf(it.x, it.y, it.value) }),
            gson.toJson(heatMap.xLabels),
            gson.toJson(heatMap.yLabels)
        )
    }
}