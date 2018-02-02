package cs.ut.engine

import cs.ut.config.MasterConfiguration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


object NirdizatiThreadPool {
    private lateinit var threadPool: ThreadPoolExecutor

    init {
        initPool()
    }

    fun execute(runnable: Runnable): Future<*> = threadPool.submit(runnable)

    fun shutDown() = threadPool.shutdown()

    fun restart() {
        shutDown()
        initPool()
    }

    private fun initPool() {
        val config = MasterConfiguration.threadPoolConfiguration
        threadPool = ThreadPoolExecutor(
            config.core,
            config.max,
            config.keepAlive.toLong(),
            TimeUnit.SECONDS,
            ArrayBlockingQueue<Runnable>(config.capacity)
        )
    }
}