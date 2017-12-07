package cs.ut.charts

import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.util.Clients

class LineChart(name: String, payload: String, private val numberOfEvents: Int) : Chart(name, payload) {
    override fun render() {
        Clients.evalJavaScript("plot_line('$payload','${Labels.getLabel(getCaption())}','$numberOfEvents')")
    }
}