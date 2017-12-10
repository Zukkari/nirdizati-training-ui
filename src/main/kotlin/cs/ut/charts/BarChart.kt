package cs.ut.charts

import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.util.Clients

class BarChart(name: String, payload: String, private val labels: String) : Chart(name, payload) {
    override fun getCaption(): String = name

    override fun render() {
        Clients.evalJavaScript("plot_bar('$payload','${NirdizatiUtil.localizeText(getCaption())}', '$labels')")
    }
}