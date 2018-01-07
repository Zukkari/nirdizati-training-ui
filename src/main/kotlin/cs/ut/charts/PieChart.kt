package cs.ut.charts

import org.zkoss.zk.ui.util.Clients

class PieChart(name: String, payload: String) : Chart(name, payload), Renderable {
    override fun getCaption(): String = name

    override fun render() {
        Clients.evalJavaScript("plot_pie('$payload')")
    }
}