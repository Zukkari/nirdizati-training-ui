package cs.ut.charts

import cs.ut.util.NirdizatiTranslator
import org.zkoss.zk.ui.util.Clients

/**
 * Bar chart data server side representation (e.g. feature importance charts)
 */
class BarChart(name: String, payload: String, private val labels: String) : Chart(name, payload) {
    override fun getCaption(): String = name

    override fun render() {
        Clients.evalJavaScript("barChart('$payload','${NirdizatiTranslator.localizeText(getCaption())}', '$labels')")
    }
}