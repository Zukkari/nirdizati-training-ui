package cs.ut.engine

import cs.ut.configuration.ConfigurationReader
import cs.ut.logging.NirdizatiLogger
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Thread pool that executes jobs for Nirdizati Training
 */
object NirdizatiThreadPool {
    private val log = NirdizatiLogger.getLogger(NirdizatiLogger::class.java)

    private lateinit var threadPool: ThreadPoolExecutor

    private val configNode = ConfigurationReader.findNode("threadPool")

    init {
        log.debug("Initializing thread pool")
        initPool()
    }

    /**
     * Execute a runnable in this thread pool
     *
     * @param runnable to execute
     *
     * @return future to control the job status
     */
    fun execute(runnable: Runnable): Future<*> = threadPool.submit(runnable)

    /**
     * Gracefully shut down threadpool
     */
    fun shutDown() = threadPool.shutdown()

    /**
     * Restart thread pool
     */
    fun restart() {
        shutDown()
        initPool()
    }

    /**
     * Thread pool initialization
     */
    private fun initPool() {
        threadPool = ThreadPoolExecutor(
            configNode.valueWithIdentifier("core").intValue(),
            configNode.valueWithIdentifier("max").intValue(),
            configNode.valueWithIdentifier("keepAlive").intValue().toLong(),
            TimeUnit.SECONDS,
            ArrayBlockingQueue<Runnable>(configNode.valueWithIdentifier("capacity").intValue())
        )

        Runtime.getRuntime().addShutdownHook(Thread {
            log.debug("Shutdown hook triggered, shutting down thread pool")
            threadPool.shutdown()
            log.debug("Thread pool successfully shut down")
        })
    }
}