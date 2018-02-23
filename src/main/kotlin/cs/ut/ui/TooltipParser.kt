package cs.ut.ui

import cs.ut.configuration.ConfigurationReader
import org.zkoss.zul.Html
import org.zkoss.zul.Popup


class TooltipParser {
    private val configNode = ConfigurationReader.findNode("tooltip/tooltips")!!

    fun readTooltip(id: String): Popup {
        val popup = Popup()

        configNode.childNodes.firstOrNull { it.identifier == id }?.apply tooltip@ {
            popup.id = id
            popup.appendChild(
                Html(this.valueWithIdentifier("label").value).apply { this.id = id })
        }

        return popup
    }
}