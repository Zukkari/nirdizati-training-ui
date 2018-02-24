package cs.ut.charts

import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.util.Clients

/**
 * Scatter plot sever side data representation for client side scatter plot
 */
class ScatterPlot(name: String, payload: String) : Chart(name, payload) {

    override fun render() {
        Clients.evalJavaScript("scatterPlot('$payload','${NirdizatiUtil.localizeText(getCaption())}')")
    }
}