package cs.ut.charts

import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.util.Clients

class ScatterPlot(name: String, payload: String) : Chart(name, payload) {
    override fun render() {
        Clients.evalJavaScript("plot_scatter('$payload','${Labels.getLabel(getCaption())}')")
    }
}