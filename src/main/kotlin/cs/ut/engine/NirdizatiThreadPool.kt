package cs.ut.engine

import cs.ut.config.MasterConfiguration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


object NirdizatiThreadPool {
    private val threadPool: ThreadPoolExecutor

    init {
        val config = MasterConfiguration.threadPoolConfiguration
        threadPool = ThreadPoolExecutor(
            config.core,
            config.max,
            config.keepAlive.toLong(),
            TimeUnit.SECONDS,
            ArrayBlockingQueue<Runnable>(config.capacity)
        )
    }

    fun execute(runnable: Runnable): Future<*> = threadPool.submit(runnable)
}