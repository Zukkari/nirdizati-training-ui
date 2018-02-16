package cs.ut.ui

import cs.ut.config.MasterConfiguration
import org.zkoss.zul.Html
import org.zkoss.zul.Label
import org.zkoss.zul.Popup
import java.nio.charset.Charset
import java.util.Base64


class TooltipParser {
    private val config = MasterConfiguration.tooltipConfig

    fun readTooltip(id: String): Popup {
        val popup = Popup()

        config.items.firstOrNull { it.id == id }?.apply tooltip@ {
            popup.id = id
            popup.appendChild(
                if (this.enableHtml) {
                    Html(this.label).apply {
                        this.id = this@tooltip.id
                    }
                } else {
                    Label(this.label)
                }
            )
        }

        return popup
    }
}