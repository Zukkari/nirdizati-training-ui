package cs.ut.jobs

import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.util.ACTIVITY_COL
import cs.ut.util.CASE_ID_COL
import cs.ut.util.FileWriter
import cs.ut.util.TIMESTAMP_COL
import org.apache.commons.io.FilenameUtils
import org.json.JSONObject
import org.zkoss.zk.ui.Desktop
import java.io.File
import java.util.*

class DataSetGenerationJob(
        val parameters: MutableMap<String, MutableList<String>>,
        currentFile: File, client: Desktop) : Job(client) {

    override var startTime = Date()
    override var completeTime = Date()

    private var json: JSONObject = JSONObject()
    private var fileName = FilenameUtils.getBaseName(currentFile.name)

    override fun preProcess() {
        json.put(CASE_ID_COL, parameters.remove(CASE_ID_COL)!![0])
        json.put(TIMESTAMP_COL, parameters.remove(TIMESTAMP_COL)!![0])
        json.put(ACTIVITY_COL, parameters.remove(ACTIVITY_COL)!![0])

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