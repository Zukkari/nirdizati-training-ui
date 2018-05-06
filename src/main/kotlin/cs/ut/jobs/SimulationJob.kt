package cs.ut.jobs

import cs.ut.configuration.ConfigurationReader
import cs.ut.exceptions.Left
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.exceptions.Right
import cs.ut.exceptions.perform
import cs.ut.jobs.UserRightsJob.Companion.updateACL
import cs.ut.json.JSONHandler
import cs.ut.json.JobInformation
import cs.ut.json.TrainingConfiguration
import cs.ut.providers.Dir
import cs.ut.providers.DirectoryConfiguration
import cs.ut.util.NirdizatiTranslator
import java.io.File


class SimulationJob(
        val configuration: TrainingConfiguration,
        val logFile: File,
        val owner: String,
        id: String = ""
) : Job(id) {

    private var process: Process? = null
    private val configNode = ConfigurationReader.findNode("userPreferences")

    override fun preProcess() {
        log.debug("Generating training parameters for job $this")
        configuration.info = JobInformation(owner, logFile.absolutePath, startTime)

        JSONHandler().writeToFile(configuration, "$id.json", Dir.TRAIN_DIR).apply {
            updateACL(this)
        }
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
        parameters.add(id)


        val execRes = perform {
            val pb = ProcessBuilder(parameters)
            pb.inheritIO()

            pb.directory(File(DirectoryConfiguration.dirPath(Dir.CORE_DIR)))

            val env = pb.environment()
            env["PYTHONPATH"] = DirectoryConfiguration.dirPath(Dir.SCRIPT_DIR)

            log.debug("Script call: ${pb.command()}")
            process = pb.start()

            log.debug("Waiting for process completion")
            process!!.waitFor()
            log.debug("Script finished running...")

            val file = File(DirectoryConfiguration.dirPath(Dir.PKL_DIR)).listFiles().firstOrNull { it.name.contains(this.id) }
            log.debug(file)

            if (file?.exists() == false) {
                status = JobStatus.FAILED
                throw NirdizatiRuntimeException("Script failed to write model to disk, job failed")
            } else {
                log.debug("Script exited successfully")
                process?.destroy()
            }
        }

        when (execRes) {
            is Right -> log.debug("Operation completed successfully")
            is Left -> throw NirdizatiRuntimeException("Script execution failed", execRes.error)
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

    override fun onError() {
        super.onError()
        log.debug("Handling error for job $id")
        val file = File(DirectoryConfiguration.dirPath(Dir.TRAIN_DIR) + "$id.json")
        if (file.exists()) {
            log.debug("File ${file.name} exists, deleting file")
            val deleted = file.delete()
            log.debug("File deleted successfully ? $deleted")
        }
        log.debug("Finished handling error for $id")
    }

    override fun isNotificationRequired() = true
    override fun getNotificationMessage() = NirdizatiTranslator.localizeText("job.completed.simulation", this.toString())

    override fun errorOccurred(): Boolean {
        log.debug("Process exit value = ${process?.exitValue()}")
        return process?.exitValue() != 0
    }

    override fun toString(): String {
        return logFile.nameWithoutExtension +
                "_" +
                configuration.bucketing.parameter +
                "_" +
                configuration.encoding.parameter +
                "_" +
                configuration.learner.parameter +
                "_" +
                configuration.outcome.parameter +
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