package cs.ut.config.items

import cs.ut.jobs.SimulationJob
import cs.ut.manager.LogManager
import cs.ut.ui.getScatterPayload
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.SerializableEventListener
import org.zkoss.zk.ui.util.Clients

enum class ChartType {
    scatter,
    line,
    bar
}

class ChartData(val caption: String, val action: SerializableEventListener<Event>)

class ChartDataDelegate(val job: SimulationJob?) {
    private val namespace = "chart_data."

    fun getCharts(): List<ChartData> = if (job == null) listOf() else listOf(
            /* True vs predicted value */
            ChartData(namespace + "true_vs_predicted", SerializableEventListener { _ ->
                val payload = getScatterPayload(LogManager.getInstance().getDetailedFile(job))
                val type = ChartType.scatter.name
                val caption = namespace + "true_vs_predicted"
                evalJs(payload, caption, ChartType.scatter)
            })
    )

    private fun evalJs(payload: String, caption: String, type: ChartType) {
        when (type) {
            ChartType.scatter -> Clients.evalJavaScript("plot_scatter('$payload','${Labels.getLabel(caption)}')")
            ChartType.line -> Clients.evalJavaScript("plot_line('$payload','${Labels.getLabel(caption)}')")
            ChartType.bar -> Clients.evalJavaScript("plot_bar('$payload','${Labels.getLabel(caption)}')")
        }
    }
}