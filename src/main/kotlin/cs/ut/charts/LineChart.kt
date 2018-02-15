package cs.ut.charts

import org.zkoss.zk.ui.util.Clients

class LineChart(val id: String, name: String, payload: String, private val numberOfEvents: Int) : Chart(name, payload) {
    override fun render() {
        Clients.evalJavaScript(
            "lineChart('$payload','$id','$numberOfEvents', '${name.toUpperCase()}')"
        )
    }
}