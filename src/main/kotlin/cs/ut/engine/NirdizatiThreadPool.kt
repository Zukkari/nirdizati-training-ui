package cs.ut.engine

import cs.ut.configuration.ConfigurationReader
import cs.ut.jobs.Job
import cs.ut.logging.NirdizatiLogger
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

/**
 * Thread pool that executes jobs for Nirdizati Training
 */
object NirdizatiThreadPool : ServletContextListener {
    private val log = NirdizatiLogger.getLogger(NirdizatiLogger::class.java)

    internal lateinit var threadPool: ExecutorService

    internal val configNode = ConfigurationReader.findNode("threadPool")

    /**
     * Execute a runnable in this thread pool
     *
     * @param runnable to execute
     *
     * @return future to control the job status
     */
    fun execute(runnable: Runnable): Future<*> = threadPool.submit(runnable)

    fun runStartUpRoutine() {
        val node = configNode.childNodes.first { it.identifier == "onStartUp" }

        val pkg = node.valueWithIdentifier("package").value
        log.debug("Looking for jobs in package: $pkg")
        val jobs = node.itemListValues()
        log.debug("Jobs to execute on start up -> ${jobs.size}")

        jobs.forEach {
            log.debug("Executing job $it")
            val instance = Class.forName("$pkg.$it").newInstance() as Job
            JobManager.runServiceJob(instance)
            log.debug("Job submitted to worker")
        }
    }
}

@WebListener
class NirdizatiPoolContext : ServletContextListener {
    private val log = NirdizatiLogger.getLogger(NirdizatiPoolContext::class.java)

    override fun contextInitialized(sce: ServletContextEvent?) {
        configureLogger()

        log.debug("Initializing thread pool")
        val size = NirdizatiThreadPool.configNode.valueWithIdentifier("capacity").intValue()
        log.debug("Thread pool size: $size")
        NirdizatiThreadPool.threadPool = Executors.newFixedThreadPool(size, { runnable -> Thread(runnable) })

        NirdizatiThreadPool.runStartUpRoutine()
        log.debug("Finished thread pool initialization")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        log.debug("Shutting down thread pool")
        NirdizatiThreadPool.threadPool.shutdown()
        log.debug("Thread pool successfully stopped")
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