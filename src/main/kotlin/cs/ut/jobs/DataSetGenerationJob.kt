package cs.ut.jobs

import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.providers.Dir
import cs.ut.providers.DirectoryConfiguration
import cs.ut.util.Columns
import cs.ut.util.FileWriter
import cs.ut.util.IdentColumns
import org.json.JSONObject
import java.io.File

/**
 * Generates data set for a job
 *
 * @param parameters to generate the JSON file from
 * @param currentFile file name to include in JSON
 */
class DataSetGenerationJob(
    val parameters: MutableMap<String, MutableList<String>>,
    currentFile: File
) : Job() {

    private val json: JSONObject = JSONObject()
    private val fileName = currentFile.nameWithoutExtension

    @Suppress("UNCHECKED_CAST")
    override fun preProcess() {
        for (col in IdentColumns.values()) {
            if (col != IdentColumns.RESOURCE) {
                json.put(col.value, parameters.remove(col.value)!![0])
            }
        }

        // Resource column should always be dynamic categorical
        parameters[Columns.DYNAMIC_CAT_COLS.value]?.apply {
            val resource = parameters.remove(IdentColumns.RESOURCE.value)!![0]
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