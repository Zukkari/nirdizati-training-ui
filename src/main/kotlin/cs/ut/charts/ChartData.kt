package cs.ut.charts

import com.google.gson.Gson
import cs.ut.jobs.SimulationJob
import cs.ut.manager.LogManager
import java.io.File

class ChartGenerator(val job: SimulationJob?) {
    companion object {
        const val TRUE_VS_PREDICTED = "true_vs_predicted"
        const val MAE_VS_EVENTS = "number_vs_mae"
        const val FEATURES = "feature_importance"
    }

    private val gson = Gson()

    fun getCharts(): List<Chart> {
        val charts = mutableListOf<Chart>()

        charts.add(generateScatterPlot(TRUE_VS_PREDICTED))
        charts.add(generateLineChart(MAE_VS_EVENTS))
        charts.addAll(generateBarCharts(FEATURES))

        return charts
    }

    private fun generateScatterPlot(name: String): ScatterPlot {
        val payload = getLinearPayload(LogManager.getInstance().getDetailedFile(job), Mode.SCATTER)
        return ScatterPlot(name, gson.toJson(payload))
    }

    private fun generateLineChart(name: String): LineChart {
        val payload = getLinearPayload(LogManager.getInstance().getValidationFile(job), Mode.LINE)
        return LineChart(name, gson.toJson(payload), payload.last().x.toInt())
    }

    private fun generateBarCharts(name: String): List<BarChart> {
        val files = LogManager.getInstance().getFeatureImportanceFiles(job)
        val charts = mutableListOf<BarChart>()

        files.forEach {
            val payload = getBarChartPayload(it)
            charts.add(BarChart(name, gson.toJson(payload.map { it.value }), gson.toJson(payload.map { it.label })))
        }

        return charts.toList()
    }
}