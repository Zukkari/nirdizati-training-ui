package cs.ut.configuration

import org.apache.log4j.ConsoleAppender
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout

object ConfigurationReader {
    private var configuration = Configuration.readSelf()

    private const val pathDelimiter = "/"

    init {
        configureLogger()
    }

    fun reload() {
        configuration = Configuration.readSelf()
    }

    fun findNode(path: String): ConfigNode? {
        var currentNode: ConfigNode? = null

        path.split(pathDelimiter).forEach { p ->
            val nodes = if (currentNode == null) configuration.childNodes else currentNode!!.childNodes
            currentNode = nodes.first { it.identifier == p }
        }

        return currentNode ?: throw NoSuchElementException("Node with path $path could not be found")
    }

    /**
     * Configures logger and Enables appenders for Log4j
     */
    private fun configureLogger() {
        Logger.getRootLogger().level = Level.DEBUG
        Logger.getRootLogger().removeAllAppenders()
        Logger.getRootLogger().additivity = false

        val ca = ConsoleAppender()
        ca.layout = PatternLayout("<%d{ISO8601}> <%p> <%F:%L> <%m>%n")
        ca.threshold = Level.DEBUG
        ca.activateOptions()

        Logger.getRootLogger().addAppender(ca)

        val fileAppender = FileAppender()
        fileAppender.layout = PatternLayout("<%d{ISO8601}> <%p> <%F:%L> <%m>%n")
        fileAppender.name = "nirdizati_ui_log.log"
        fileAppender.file = "nirdizati_ui_log.log"
        fileAppender.threshold = Level.DEBUG
        fileAppender.append = true
        fileAppender.activateOptions()

        Logger.getRootLogger().addAppender(fileAppender)
    }
}