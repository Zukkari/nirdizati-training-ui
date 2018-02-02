package cs.ut.ui

import cs.ut.config.MasterConfiguration
import org.zkoss.zul.Html
import org.zkoss.zul.Label
import org.zkoss.zul.Popup
import java.nio.charset.Charset
import java.util.*

class TooltipParser {
    private val config = MasterConfiguration.tooltipConfig

    fun readTooltip(id: String): Popup {
        val popup = Popup()

        config.items.firstOrNull { it.id == id }?.apply tooltip@ {
            popup.appendChild(
                if (this.isHtml) {
                    Html(decodeBase64(this.label)).apply {
                        this.id = this@tooltip.id
                    }
                } else {
                    Label(decodeBase64(this.label))
                }
            )
        }

        return popup
    }

    private fun decodeBase64(payload: String) = String(Base64.getDecoder().decode(payload), Charset.forName("UTF-8"))
}