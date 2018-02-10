package cs.ut.engine

import cs.ut.config.MasterConfiguration
import cs.ut.config.UiData
import cs.ut.config.nodes.Dir
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.SimulationJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.util.LOG_FILE
import cs.ut.util.OWNER
import cs.ut.util.PREFIX
import cs.ut.util.START_DATE
import cs.ut.util.UI_DATA
import org.apache.commons.io.FilenameUtils
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.util.Date

object LogManager {
    private val log = NirdizatiLogger.getLogger(LogManager::class.java)

    private const val REGRESSION = "_regr"
    private const val CLASSIFICATION = "_class"
    private const val DETAILED = "detailed_"
    private const val FEATURE = "feat_importance_"
    private const val VALIDATION = "validation_"

    private val allowedExtensions: List<String>

    private val logDirectory: String
    private val validationDir: String
    private val featureImportanceDir: String
    private val detailedDir: String

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
    fun getAllAvailableLogs(): List<File> =
        File(logDirectory).listFiles().filter { it.extension in allowedExtensions }


    /**
     * Returns file from configured detailed directory that is made as result of given job
     *
     * @param job for which job file should be retrieved
     * @return file that contains job results
     */
    fun getDetailedFile(job: SimulationJob): File {
        log.debug("Getting detailed log information for job '$job'")
        return getFile(detailedDir + job.getFileName(DETAILED))
    }

    /**
     * Returns file from configured validation directory that is made as result of given job
     *
     * @param job for which job file should be retrieved
     * @return file that contains job results
     */
    fun getValidationFile(job: SimulationJob): File {
        log.debug("Getting validation log file for job '$job'")
        return getFile(validationDir + job.getFileName(VALIDATION))
    }

    fun getFeatureImportanceFiles(job: SimulationJob): List<File> {
        log.debug("Getting feature importance log information for job: '$job'")
        if (PREFIX == job.bucketing.id) {
            log.debug("Prefix job, looking for all possible files for this job")

            val files = mutableListOf<File>()
            (1..15).forEach { i ->
                try {
                    files.add(getFile(featureImportanceDir + job.getFileName(FEATURE) + "_$i"))
                } catch (e: Exception) {
                    log.debug("Found ${files.size} files for job: $job")
                    return files
                }
            }
            log.debug("Found ${files.size} files for job: $job")
            return files
        } else {
            return listOf(getFile(featureImportanceDir + job.getFileName(FEATURE) + "_1"))
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

    /**
     * Returns whether given job is classification or regression
     * @param job that needs to be categorized
     */
    fun isClassification(job: SimulationJob): Boolean =
        !File(detailedDir + DETAILED + FilenameUtils.getBaseName(job.logFile.name) + "_" + job.id + REGRESSION + ".csv").exists()

    private fun SimulationJob.getFileName(dir: String): String =
        if (dir == FEATURE)
            dir + this.logFile.nameWithoutExtension + "_" + this.id
        else
            dir + this.logFile.nameWithoutExtension + "_" + this.id + if (isClassification(this)) CLASSIFICATION else REGRESSION

    fun loadJobIds(key: String): List<UiData> {
        return mutableListOf<UiData>().also { c ->
            loadTrainingFiles().forEach {
                val uiData = JSONObject(readFileContent(it)).getJSONObject(UI_DATA)
                if (uiData[OWNER] == key) {
                    c.add(UiData(it.nameWithoutExtension, uiData[LOG_FILE] as String, startDate(uiData)))
                }
            }
        }
    }

    private fun readFileContent(f: File): String = BufferedReader(FileReader(f)).readLines().joinToString()


    private fun loadTrainingFiles(): List<File> {
        log.debug("Loading training files")
        val dir: File = MasterConfiguration.dirConfig.dirByName(Dir.TRAIN_DIR)
        log.debug("Looking for training files in ${dir.absolutePath}")
        val files = dir.listFiles() ?: arrayOf()
        log.debug("Found ${files.size} training files total")
        return files.toList()
    }

    private fun startDate(data: JSONObject): Instant {
        return Instant.parse(data[START_DATE] as String)
    }
}