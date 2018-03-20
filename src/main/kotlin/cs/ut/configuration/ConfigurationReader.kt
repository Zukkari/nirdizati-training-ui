package cs.ut.configuration

import cs.ut.engine.JobManager
import cs.ut.jobs.StartUpJob
import org.apache.log4j.*

/**
 * Helper object that reads traverses the configuration tree
 *
 * @see Configuration
 */
object ConfigurationReader {
    private var configuration = Configuration.readSelf()

    private const val pathDelimiter = "/"

    init {
        configureLogger()
        JobManager.runServiceJob(StartUpJob())
    }

    /**
     * Reloads the configuration into memory
     */
    fun reload() {
        configuration = Configuration.readSelf()
    }

    /**
     * Find configuration node based on given path.
     * Path should be delimited using delimiter as the "pathDelimiter" value
     *
     * @param path to look for
     * @return configuration node corresponding to given path
     */
    fun findNode(path: String): ConfigNode {
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