package cs.ut.jobs

import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.util.FileWriter
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PREFIX
import org.apache.commons.io.FilenameUtils
import org.json.JSONObject
import org.zkoss.zk.ui.Desktop
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class SimulationJob(
        val encoding: ModelParameter,
        val bucketing: ModelParameter,
        val learner: ModelParameter,
        val outcome: ModelParameter,
        val isClassification: Boolean,
        val logFile: File,
        client: Desktop) : Job(client) {

    override var startTime = Date()
    override var completeTime = Date()

    private var process: Process? = null

    override fun preProcess() {
        log.debug("Generating training parameters for job $this")
        val json = JSONObject()

        val params = JSONObject()

        if (bucketing.id == PREFIX) {
            val props = JSONObject()
            learner.properties.forEach { (k, _, v) -> props.put(k, convertToNumber(v)) }
            for (i in 1..15) {
                params.put(i.toString(), props)
            }
        } else {
            learner.properties.forEach { (k, _, v) -> params.put(k, convertToNumber(v)) }
        }

        json.put(outcome.parameter,
                JSONObject().put(bucketing.parameter + "_" + encoding.parameter,
                        JSONObject().put(learner.parameter, params)
                )
        )

        val writer = FileWriter()
        writer.writeJsonToDisk(json, FilenameUtils.getBaseName(logFile.name),
                MasterConfiguration.directoryPathConfiguration.trainDirectory)
    }

    override fun execute() {
        try {
            val pb = ProcessBuilder(
                    MasterConfiguration.directoryPathConfiguration.python,
                    "train.py",
                    logFile.name,
                    bucketing.parameter,
                    encoding.parameter,
                    learner.parameter,
                    outcome.parameter
            )

            pb.directory(File(coreDir))
            pb.inheritIO()

            val env = pb.environment()
            env.put("PYTHONPATH", scriptDir)

            log.debug("Script call: ${pb.command()}")
            process = pb.start()
            if (!process!!.waitFor(180, TimeUnit.SECONDS) || stop) {
                status = JobStatus.FAILED
                process!!.destroy()
                log.debug("Stopping script -> stop: $stop")
                return
            }

            log.debug("Script finished running...")

            val file = File(scriptDir + pklDir + this.toString())
            log.debug(file)

            if (!file.exists()) {
                status = JobStatus.FAILED
                throw NirdizatiRuntimeException("Script failed to write model to disk, job failed")
            } else {
                log.debug("Script exited successfully")
            }
        } catch (e: IOException) {
            throw NirdizatiRuntimeException("Script execution failed", e)
        } catch (e: InterruptedException) {
            throw NirdizatiRuntimeException("Script execution failed", e)
        }
    }

    override fun kill() {
        process?.destroy()
        stop = true
    }

    override fun isNotificationRequired() = true
    override fun getNotificationMessage() = NirdizatiUtil.localizeText("job.completed.simulation", this.toString())

    override fun toString(): String {
        return FilenameUtils.getBaseName(logFile.name) +
                "_" +
                bucketing.parameter +
                "_" +
                encoding.parameter +
                "_" +
                learner.parameter +
                "_" +
                outcome.parameter +
                ".pkl"
    }

    private fun convertToNumber(value: String): Number =
            try {
                value.toInt()
            } catch (e: NumberFormatException) {
                value.toDouble()
            }
}