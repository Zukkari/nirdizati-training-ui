package cs.ut.charts

import com.google.gson.Gson
import cs.ut.jobs.SimulationJob
import cs.ut.manager.LogManager

class ChartGenerator(val job: SimulationJob?) {
    companion object {
        const val TRUE_VS_PREDICTED = "true_vs_predicted"
        const val FEATURES = "feature_importance"
    }

    private val gson = Gson()

    fun getCharts(): List<Chart> {
        val charts = mutableListOf<Chart>()

        charts.add(generateScatterPlot(TRUE_VS_PREDICTED))
        charts.addAll(generateLineCharts())
        charts.addAll(generateBarCharts(FEATURES))

        return charts
    }

    private fun generateScatterPlot(name: String): ScatterPlot {
        val payload = getLinearPayload(LogManager.getInstance().getDetailedFile(job), Mode.SCATTER)
        return ScatterPlot(name, gson.toJson(payload))
    }

    private fun generateLineCharts(): List<LineChart> {
        val payload = getLinearPayload(LogManager.getInstance().getValidationFile(job), Mode.LINE).groupBy { it.dataType }
        var charts = listOf<LineChart>()
        payload.forEach { charts += LineChart(it.key, gson.toJson(it.value), it.value.last().x.toInt()) }
        return charts
    }

    private fun generateBarCharts(name: String): List<BarChart> {
        val files = LogManager.getInstance().getFeatureImportanceFiles(job)
        val charts = mutableListOf<BarChart>()

        (1..files.size).zip(files).forEach {
            val payload = getBarChartPayload(it.second)
            charts.add(BarChart(it.first.toString(), gson.toJson(payload.map { it.value }), gson.toJson(payload.map { it.label })))
        }

        return charts.toList()
    }
}