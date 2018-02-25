package cs.ut.charts

import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.util.Clients

/**
 * Heat map data server side representation for heat map chart on client side.
 */
class HeatMap(name: String, payload: String, private val xLabels: String, private val yLabels: String) :
    Chart(name, payload) {

    override fun render() {
        Clients.evalJavaScript(
            "heatMap('$payload', " +
                    "'${NirdizatiUtil.localizeText(getCaption())}'" +
                    ",'$xLabels'" +
                    ", '$yLabels')"
        )
    }
}