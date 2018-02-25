package cs.ut.logging

import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Logger wrapper that logs data with specific tag so it is easier to track different UI actions
 */
class NirdizatiLogger(name: String) : Logger(name) {
    var id: String = "GLOBAL"

    override fun debug(message: Any?, t: Throwable?) {
        super.log(FQCN, Level.DEBUG, "[$id] $message", t)
    }

    override fun debug(message: Any?) {
        super.log(FQCN, Level.DEBUG, "[$id] $message", null)
    }

    override fun error(message: Any?) {
        super.log(FQCN, Level.ERROR, "[$id] $message", null)
    }

    override fun error(message: Any?, t: Throwable?) {
        super.log(FQCN, Level.ERROR, "[$id] $message", t)
    }

    override fun info(message: Any?) {
        super.log(FQCN, Level.INFO, "[$id] $message", null)
    }

    override fun info(message: Any?, t: Throwable?) {
        super.log(FQCN, Level.INFO, "[$id] $message", t)
    }

    companion object {
        fun getLogger(clazz: Class<*>, id: String = "GLOBAL") =
            (Logger.getLogger(clazz.name, factory) as NirdizatiLogger).apply { this.id = id }

        @JvmStatic
        private val factory = NirdizatiLoggerFactory()

        @JvmStatic
        private val FQCN = NirdizatiLogger::class.java.name
    }
}