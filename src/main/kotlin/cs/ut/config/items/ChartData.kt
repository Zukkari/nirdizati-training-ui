package cs.ut.config.items

import com.google.gson.Gson
import cs.ut.jobs.SimulationJob
import cs.ut.manager.LogManager
import cs.ut.ui.Mode
import cs.ut.ui.getBarChartPayload
import cs.ut.ui.getLinearPayload
import cs.ut.util.PREFIX
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.SerializableEventListener
import org.zkoss.zk.ui.util.Clients
import java.io.File

enum class ChartType {
    scatter,
    line,
    bar
}

class ChartData(val caption: String, val action: SerializableEventListener<Event>)

class ChartDataDelegate(val job: SimulationJob?) {
    private val namespace = "chart_data."
    private val gson = Gson()

    fun getCharts(): List<ChartData> {
        if (job == null) return listOf()
        else {
            val charts = mutableListOf(
                    /* True vs predicted value */
                    ChartData(namespace + "true_vs_predicted", SerializableEventListener { _ ->
                        val payload = getLinearPayload(LogManager.getInstance().getDetailedFile(job), Mode.SCATTER)
                        val caption = namespace + "true_vs_predicted"
                        evalJs(gson.toJson(payload), caption, ChartType.scatter)
                    }),

                    /* Number of events vs MAE */
                    ChartData(namespace + "number_vs_mae", SerializableEventListener { _ ->
                        val payload = getLinearPayload(LogManager.getInstance().getValidationFile(job), Mode.LINE)
                        val caption = namespace + "number_vs_mae"
                        evalJs(gson.toJson(payload), caption, ChartType.line, payload.last().x.toInt())
                    })
            )

            /* Feature importance */
            if (job.bucketing.id == PREFIX) {
                (0..14).mapTo(charts) { i ->
                    ChartData(namespace + "feature_importance", SerializableEventListener { _ ->
                        val files: List<File> = LogManager.getInstance().getFeatureImportanceFiles(job) as List<File>
                        val payload = getBarChartPayload(files[i])
                        val caption = namespace + "feature_importance"
                        val gson = Gson()

                        evalJs(gson.toJson(payload.map { it.value }),
                                caption,
                                ChartType.bar,
                                labels = gson.toJson(payload.map { it.label }))
                    })
                }
            } else {
                charts.add(/* Feature importance */
                        ChartData(namespace + "feature_importance", SerializableEventListener { _ ->
                            val file: File = LogManager.getInstance().getFeatureImportanceFiles(job) as File
                            val payload = getBarChartPayload(file)
                            val caption = namespace + "feature_importance"
                            val gson = Gson()

                            evalJs(gson.toJson(payload.map { it.value }),
                                    caption,
                                    ChartType.bar,
                                    labels = gson.toJson(payload.map { it.label }))
                        }))
            }

            return charts
        }
    }

    private fun evalJs(payload: String, caption: String, type: ChartType, events: Int = 0, labels: String = "") {
        when (type) {
            ChartType.scatter -> Clients.evalJavaScript("plot_scatter('$payload','${Labels.getLabel(caption)}')")
            ChartType.line -> Clients.evalJavaScript("plot_line('$payload','${Labels.getLabel(caption)}','$events')")
            ChartType.bar -> Clients.evalJavaScript("plot_bar('$payload','${Labels.getLabel(caption)}', '$labels')")
        }
    }
}