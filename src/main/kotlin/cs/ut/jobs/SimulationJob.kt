package cs.ut.jobs

import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.util.FileWriter
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.json.JSONObject
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.TimeUnit

class SimulationJob(val encoding: ModelParameter,
                    val bucketing: ModelParameter,
                    val learner: ModelParameter,
                    val outcome: ModelParameter,
                    val logFile: File,
                    client: Desktop) : Job(client) {

    val log = Logger.getLogger(SimulationJob::class.java)!!

    override var startTime = Date()
    override var completeTime = Date()

    override fun preProcess() {
        log.debug("Generating training parameters for job $this")
        val json = JSONObject()

        val params = JSONObject()
        learner.properties.forEach { (k, _, v) -> params.put(k, convertToNumber(v)) }

        json.put(outcome.parameter,
                JSONObject().put(bucketing.parameter + "_" + encoding.parameter,
                        JSONObject().put(learner.parameter, params)
                )
        )

        val writer = FileWriter()
        writer.writeJsonToDisk(json, FilenameUtils.getBaseName(logFile.name),
                MasterConfiguration.getInstance().directoryPathConfiguration.trainDirectory)
    }

    override fun execute() {
        try {
            val pb = ProcessBuilder(
                    "python",
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

            val process = pb.start()
            log.debug("Script call: ${pb.command()}")
            if (!process.waitFor(180, TimeUnit.SECONDS)) {
                process.destroy()
                log.debug("Timed out while executing script")
            }

            log.debug("Script finished running...")

            val file = File(scriptDir + pklDir + this.toString())
            log.debug(file)

            if (!file.exists()) {
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

    override fun postExecute() {
        log.debug("Moving file to user model storage directory <$userModelDir>")

        val noExtensionName = FilenameUtils.getBaseName(log.name)
        val dir = File(userModelDir + noExtensionName)

        if (!dir.exists() && !dir.mkdir()) log.debug("Cannot create folder for model with name ${dir.name}")

        try {
            Files.move(Paths.get(scriptDir + pklDir + this.toString()),
                    Paths.get(userModelDir + noExtensionName + "/" + this.toString()), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            throw NirdizatiRuntimeException("Script execution failed", e)
        }
    }

    override fun isNotificationRequired() = true
    override fun getNotificationMessage() = Labels.getLabel("job.completed.simulation", arrayOf(this.toString()))!!

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

    private fun convertToNumber(value: String): Number {
        try {
            return value.toInt()
        } catch (e: NumberFormatException) {
            return value.toDouble()
        }
    }
}