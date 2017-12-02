package cs.ut.engine

import cs.ut.config.MasterConfiguration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class NirdizatiThreadPool() {
    companion object {
        val config = MasterConfiguration.getInstance().threadPoolConfiguration
        val threadPool = ThreadPoolExecutor(config.core,
                config.max,
                config.keepAlive.toLong(),
                TimeUnit.SECONDS,
                ArrayBlockingQueue<Runnable>(config.capacity))
    }

    fun execute(runnable: Runnable) {
        threadPool.execute(runnable)
    }
}