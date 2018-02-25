package cs.ut.charts

import org.zkoss.zk.ui.util.Clients

/**
 * Line chart server side data representation for client side line chart (e.g. accuracy comparison)
 */
class LineChart(val id: String, name: String, payload: String, private val numberOfEvents: Int) : Chart(name, payload) {

    override fun render() {
        Clients.evalJavaScript(
            "lineChart('$payload','$id','$numberOfEvents', '${name.toUpperCase()}')"
        )
    }
}