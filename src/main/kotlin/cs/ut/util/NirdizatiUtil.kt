package cs.ut.util

import org.zkoss.util.resource.Labels

class NirdizatiUtil {
    companion object {
        fun localizeText(text: String, vararg  args: Any): String = Labels.getLabel(text, args) ?: text
    }
}