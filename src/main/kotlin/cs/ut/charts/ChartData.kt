package cs.ut.charts

import com.google.gson.Gson
import cs.ut.engine.LogManager
import cs.ut.jobs.SimulationJob

class ChartGenerator(val job: SimulationJob) {
    companion object {
        const val TRUE_VS_PREDICTED = "true_vs_predicted"
    }

    private val gson = Gson()

    fun getCharts(): List<Chart> {
        val charts = mutableListOf<Chart>()

        if (job.isClassification) {
            charts.addAll(generatePieChart())
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
            charts.add(BarChart(it.first.toString(), gson.toJson(payload.map { it.value }), gson.toJson(payload.map { it.label })))
        }

        return charts.toList()
    }

    private fun generatePieChart(): List<PieChart> {
        val file = LogManager.getDetailedFile(job)
        val charts = mutableListOf<PieChart>()

        val payload: List<PieChartData> = getPieChartPayload(file)
        payload.forEach {
            charts.add(PieChart(it.events.toString(), gson.toJson(arrayOf(it.correct, it.miss))))
        }
        return charts.toList()
    }
}
