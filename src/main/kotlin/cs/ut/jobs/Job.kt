package cs.ut.jobs

import cs.ut.config.MasterConfiguration
import org.zkoss.zk.ui.Desktop
import java.util.*

abstract class Job(val client: Desktop) {
    var createTime: Date = Date()
    abstract var startTime: Date
    abstract var completeTime: Date
    var status: JobStatus = JobStatus.PENDING

    val pathProvider = MasterConfiguration.getInstance().directoryPathConfiguration
    protected val scriptDir = pathProvider.scriptDirectory
    protected val userModelDir = pathProvider.userModelDirectory
    protected val coreDir = scriptDir + "core/"
    protected val datasetDir = pathProvider.datasetDirectory
    protected val trainingDir = pathProvider.trainDirectory
    protected val pklDir = pathProvider.pklDirectory

    abstract fun preProcess()

    abstract fun execute()

    abstract fun postExecute()

    open fun isNotificationRequired() = false

    open fun getNotificationMessage() = ""
}


enum class JobStatus {
    PENDING,
    PREPARING,
    RUNNING,
    FINISHING,
    COMPLETED,
    FAILED
}