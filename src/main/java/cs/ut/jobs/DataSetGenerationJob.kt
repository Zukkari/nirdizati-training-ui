package cs.ut.jobs

import cs.ut.engine.CsvReader
import cs.ut.engine.FileWriter
import cs.ut.exceptions.NirdizatiRuntimeException
import org.apache.commons.io.FilenameUtils
import org.json.JSONObject
import java.io.File
import java.util.*

class DataSetGenerationJob(val parameters: MutableMap<String, List<String>>, val currentFile: File) : Job() {
    override var startTime = Date()
    override var completeTime = Date()

    private var json: JSONObject = JSONObject()
    private var fileName = FilenameUtils.getBaseName(currentFile.name)

    override fun preProcess() {
        json.put(CsvReader.CASE_ID_COL, parameters.remove(CsvReader.CASE_ID_COL)!![0])
        json.put(CsvReader.TIMESTAMP_COL, parameters.remove(CsvReader.TIMESTAMP_COL)!![0])
        json.put(CsvReader.ACTIVITY_COL, parameters.remove(CsvReader.ACTIVITY_COL)!![0])

        parameters.forEach { k, v -> json.put(k, v) }
    }

    override fun execute() {
        val writer = FileWriter()
        writer.writeJsonToDisk(json, fileName, datasetDir)
    }

    override fun postExecute() {
        val result = File(coreDir + datasetDir + fileName + ".json")
        if (!result.exists()) {
            throw NirdizatiRuntimeException("Could not write file to disk <${result.absolutePath}>")
        }
    }

}