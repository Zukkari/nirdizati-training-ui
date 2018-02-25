package cs.ut.jobs

import cs.ut.engine.item.ModelParameter
import cs.ut.configuration.ConfigurationReader
import cs.ut.providers.Dir
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.UserRightsJob.Companion.updateACL
import cs.ut.providers.DirectoryConfiguration
import cs.ut.util.FileWriter
import cs.ut.util.LOG_FILE
import cs.ut.util.NirdizatiUtil
import cs.ut.util.OWNER
import cs.ut.util.PREFIX
import cs.ut.util.START_DATE
import cs.ut.util.UI_DATA
import org.json.JSONObject
import java.io.File
import java.io.IOException


class SimulationJob(
    val encoding: ModelParameter,
    val bucketing: ModelParameter,
    val learner: ModelParameter,
    val outcome: ModelParameter,
    val logFile: File,
    val owner: String,
    id: String = ""
) : Job(id) {

    private var process: Process? = null
    private val configNode = ConfigurationReader.findNode("userPreferences")

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

        json.put(
            outcome.parameter,
            JSONObject().put(
                bucketing.parameter + "_" + encoding.parameter,
                JSONObject().put(learner.parameter, params)
            )
        )
        json.put(
            UI_DATA, JSONObject()
                .put(OWNER, owner)
                .put(LOG_FILE, logFile.absoluteFile)
                .put(START_DATE, startTime)
        )

        val writer = FileWriter()
        val f = writer.writeJsonToDisk(
            json, id,
            DirectoryConfiguration.dirPath(Dir.TRAIN_DIR)
        )

        updateACL(f)
    }

    override fun execute() {
        val python: String = DirectoryConfiguration.dirPath(Dir.PYTHON)
        val parameters = mutableListOf<String>()
        if (configNode.isEnabled()) {
            parameters.add("sudo")
            parameters.add("-u")
            parameters.add(configNode.valueWithIdentifier("userName").value)
        }

        parameters.add(python)
        parameters.add(TRAIN_PY)
        parameters.add(logFile.name)
        parameters.add(id)

        try {
            val pb = ProcessBuilder(parameters)

            pb.directory(File(DirectoryConfiguration.dirPath(Dir.CORE_DIR)))
            pb.inheritIO()

            val env = pb.environment()
            env["PYTHONPATH"] = DirectoryConfiguration.dirPath(Dir.SCRIPT_DIR)

            log.debug("Script call: ${pb.command()}")
            process = pb.start()

            log.debug("Waiting for process completion")
            process!!.waitFor()
            log.debug("Script finished running...")

            val file = File(DirectoryConfiguration.dirPath(Dir.PKL_DIR) + this.toString())
            log.debug(file)

            if (!file.exists()) {
                status = JobStatus.FAILED
                throw NirdizatiRuntimeException("Script failed to write model to disk, job failed")
            } else {
                log.debug("Script exited successfully")
                process?.destroy()
            }
        } catch (e: IOException) {
            throw NirdizatiRuntimeException("Script execution failed", e)
        } catch (e: InterruptedException) {
            throw NirdizatiRuntimeException("Script execution failed", e)
        }
    }

    override fun beforeInterrupt() {
        log.debug("Process ${super.id} has been stopped by the user")
        process?.destroy()
        if (status != JobStatus.COMPLETED) {
            log.debug("Job is not complete -> deleting training file")
            File(DirectoryConfiguration.dirPath(Dir.TRAIN_DIR) + "$id.json").delete()
        }
    }

    override fun isNotificationRequired() = true
    override fun getNotificationMessage() = NirdizatiUtil.localizeText("job.completed.simulation", this.toString())

    override fun toString(): String {
        return logFile.nameWithoutExtension +
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

    companion object {
        const val TRAIN_PY = "train.py"
    }
}