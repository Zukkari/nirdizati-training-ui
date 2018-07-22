package cs.ut.logging

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.ConverterKeys
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter
import java.lang.StringBuilder

@Plugin(name = "connId", category = "Converter")
@ConverterKeys("connId")
class UserIdentifier : LogEventPatternConverter("userId", "userId") {

    override fun format(logEvent: LogEvent, buffer: StringBuilder) {
        val key = logEvent.contextData.getValue<String>("${logEvent.threadName}-connId") ?: ""

        buffer.append("[")
        buffer.append(if (key.isBlank()) "SYSTEM" else key)
        buffer.append("]")
    }

    companion object {
        const val CONN_ID = "connId"

        @JvmStatic
        fun newInstance(options: Array<String>): UserIdentifier {
            return UserIdentifier()
        }
    }
}