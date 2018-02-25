package cs.ut.logging

import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggerFactory

/**
 * Factory to create Nirdizati loggers
 *
 * @see NirdizatiLogger
 */
class NirdizatiLoggerFactory : LoggerFactory {

    override fun makeNewLoggerInstance(p0: String?): Logger {
        return NirdizatiLogger(p0 ?: "")
    }

}