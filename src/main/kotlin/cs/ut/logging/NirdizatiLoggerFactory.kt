package cs.ut.logging

import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggerFactory

class NirdizatiLoggerFactory : LoggerFactory {

    override fun makeNewLoggerInstance(p0: String?): Logger {
        return NirdizatiLogger(p0 ?: "")
    }

}