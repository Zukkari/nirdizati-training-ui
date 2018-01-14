package cs.ut.engine

import cs.ut.config.MasterConfiguration
import cs.ut.config.nodes.Dir
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.SimulationJob
import cs.ut.util.PREFIX
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import java.io.File

object LogManager {
    private val log: Logger = Logger.getLogger(LogManager::class.java)!!

    private const val VALIDATION = "validation_"
    private const val FEATURE = "feat_importance_"
    private const val DETAILED = "detailed_"

    private val allowedExtensions: List<String>

    private val logDirectory: String
    private val validationDir: String
    private val featureImportanceDir: String
    private val detailedDir: String

    private val useId: Boolean = MasterConfiguration.userPreferences.useId

    init {
        log.debug("Initializing $this")
        val conf = MasterConfiguration.dirConfig

        logDirectory = conf.dirPath(Dir.USER_LOGS)
        log.debug("User log directory -> $logDirectory")

        validationDir = conf.dirPath(Dir.VALIDATION_DIR)
        log.debug("Validation directory -> $validationDir")

        featureImportanceDir = conf.dirPath(Dir.FEATURE_DIR)
        log.debug("Feature importance directory -> $featureImportanceDir")

        detailedDir = conf.dirPath(Dir.DETAIL_DIR)
        log.debug("Detailed log directory -> $detailedDir")

        allowedExtensions = MasterConfiguration.csvConfiguration.extensions
    }

    /**
     * Returns all available file names contained in user log directory defined in configuration.xml
     *
     * @return List of all available file names contained in user log directory
     */
    fun getAllAvailableLogs(): List<File> = File(logDirectory).listFiles().filter { FilenameUtils.getExtension(it.name) in allowedExtensions }


    /**
     * Returns file from configured detailed directory that is made as result of given job
     *
     * @param job for which job file should be retrieved
     * @return file that contains job results
     */
    fun getDetailedFile(job: SimulationJob): File {
        log.debug("Getting detailed log information for job '$job'")
        val fileName = DETAILED + if (useId) job.id else FilenameUtils.getBaseName(job.toString())
        return getFile(detailedDir + fileName)
    }

    /**
     * Returns file from configured validation directory that is made as result of given job
     *
     * @param job for which job file should be retrieved
     * @return file that contains job results
     */
    fun getValidationFile(job: SimulationJob): File {
        log.debug("Getting validation log file for job '$job'")
        val fileName = VALIDATION + if (useId) job.id else FilenameUtils.getBaseName(job.toString())
        return getFile(validationDir + fileName)
    }

    fun getFeatureImportanceFiles(job: SimulationJob): List<File> {
        log.debug("Getting feature importance log information for job: '${job.toString()}'")
        if (PREFIX == job.bucketing.id) {
            log.debug("Prefix job, looking for all possible files for this job")

            val files = mutableListOf<File>()
            (1..15).forEach { i ->
                val fileName = featureImportanceDir + FEATURE + (if (useId) job.id else FilenameUtils.getBaseName(job.toString())) + "_$i"
                try {
                    files.add(getFile(fileName))
                } catch (e: Exception) {
                    log.debug("Found ${files.size} files for job: $job")
                    return files
                }
            }
            log.debug("Found ${files.size} files for job: $job")
            return files
        } else {
            return listOf(getFile(featureImportanceDir + FEATURE + (if (useId) job.id else FilenameUtils.getBaseName(job.toString())) + "_1"))
        }
    }

    private fun getFile(fileName: String): File {
        val file = File(fileName + ".csv")
        log.debug("Looking for file with name ${file.name}")

        if (!file.exists()) {
            throw NirdizatiRuntimeException("Result file with name ${file.absolutePath} could not be found")
        }

        log.debug("Successfully found result file with name $fileName")
        return file
    }
}