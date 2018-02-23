package cs.ut.jobs

import cs.ut.providers.Dir
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.providers.DirectoryConfiguration
import cs.ut.util.*
import org.json.JSONObject
import java.io.File

class DataSetGenerationJob(
    val parameters: MutableMap<String, MutableList<String>>,
    currentFile: File
) : Job() {

    private var json: JSONObject = JSONObject()
    private var fileName = currentFile.nameWithoutExtension

    @Suppress("UNCHECKED_CAST")
    override fun preProcess() {
        json.put(CASE_ID_COL, parameters.remove(CASE_ID_COL)!![0])
        json.put(TIMESTAMP_COL, parameters.remove(TIMESTAMP_COL)!![0])
        json.put(ACTIVITY_COL, parameters.remove(ACTIVITY_COL)!![0])

        // Resource column should always be dynamic categorical
        parameters[DYNAMIC + CAT_COLS]?.apply {
            val resource = parameters.remove(cs.ut.util.RESOURCE_COL)!![0]
            if (resource.isNotEmpty()) {
                this.add(resource)
            }
        }

        parameters.forEach { k, v -> json.put(k, v) }
    }

    override fun execute() {
        val writer = FileWriter()
        writer.writeJsonToDisk(json, fileName, DirectoryConfiguration.dirPath(Dir.DATA_DIR))
    }

    override fun postExecute() {
        val result = File(DirectoryConfiguration.dirPath(Dir.DATA_DIR) + fileName + ".json")
        if (!result.exists()) {
            throw NirdizatiRuntimeException("Could not write file to disk <${result.absolutePath}>")
        }
    }

}