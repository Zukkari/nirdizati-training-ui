package cs.ut.charts

import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.util.Clients

class BarChart(name: String, payload: String, private val labels: String) : Chart(name, payload) {
    override fun render() {
        Clients.evalJavaScript("plot_bar('$payload','${Labels.getLabel(getCaption())}', '$labels')")
    }
}