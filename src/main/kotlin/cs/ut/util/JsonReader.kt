package cs.ut.util

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import cs.ut.engine.item.ModelParameter
import cs.ut.engine.item.Property
import cs.ut.engine.item.TrainingData
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.providers.Dir
import cs.ut.providers.DirectoryConfiguration
import cs.ut.providers.ModelParamProvider
import cs.ut.ui.controllers.TrainingController.Companion.PREDICTION
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

fun readHyperParameterJson(): Map<String, List<ModelParameter>> {
    val files = readFilesFromDir()
    val modelsParams = parseJsonFiles(files)
    mapTypes(modelsParams)
    return modelsParams
}

fun readLogColumns(logName: String): List<String> {
    val json: TrainingData = readTrainingData(logName)
    return json.getAllColumns()
}

fun readTrainingJson(key: String): Map<String, List<ModelParameter>> =
        parseJsonFiles(
                listOf(
                        File(
                                DirectoryConfiguration.dirPath(Dir.TRAIN_DIR) + "$key.json"
                        )
                )
        )
                .apply { mapTypes(this) }

private fun readTrainingData(logName: String): TrainingData {
    val path: String = DirectoryConfiguration.dirPath(Dir.DATA_DIR)

    val file = File("$path$logName.json")
    val jsonReader = JsonReader(FileReader(file))

    return Gson().fromJson(jsonReader, TrainingData::class.java)
}

private fun mapTypes(modelsParams: Map<String, List<ModelParameter>>) {
    val allProperties = ModelParamProvider().getAllProperties()

    modelsParams.values.flatMap { it }.flatMap { it.properties }.forEach { prop ->
        val withType = allProperties.firstOrNull { it.id == prop.id }
        prop.type = withType?.type ?: ""
    }
}


private fun readFilesFromDir(): List<File> {
    val path = DirectoryConfiguration.dirPath(Dir.OHP_DIR)

    val dir = File(path)
    if (!dir.exists() && dir.isDirectory) throw NirdizatiRuntimeException("Optimized hyperparameter directory does not exist")

    return dir.listFiles()?.toList() ?: listOf()
}

private fun parseJsonFiles(files: List<File>): Map<String, List<ModelParameter>> {
    val map = mutableMapOf<String, List<ModelParameter>>()
    val jsons = readJsonFiles(files)
    parseJson(jsons.toMutableMap(), map)
    return map
}

private fun readJsonFiles(files: List<File>): Map<String, String> {
    val jsons = mutableMapOf<String, String>()
    readJson(files, jsons)
    return jsons
}

private tailrec fun readJson(files: List<File>, jsons: MutableMap<String, String>) {
    if (files.isNotEmpty()) {
        val file = files.first()
        try {
            val sb = StringBuilder()
            BufferedReader(FileReader(file)).use {
                it.lines().forEach { sb.append(it) }
            }
            jsons[file.nameWithoutExtension] = sb.toString()
        } catch (e: IOException) {
            throw NirdizatiRuntimeException("Could not read file $file")
        }
        readJson(files.drop(1), jsons)
    }
}

@Suppress("UNCHECKED_CAST")
private tailrec fun parseJson(jsonItems: MutableMap<String, String>, map: MutableMap<String, List<ModelParameter>>) {
    if (jsonItems.isNotEmpty()) {
        val key = jsonItems.keys.first()
        val entry = jsonItems.remove(key)
        val json = JSONObject(entry)

        val params = mutableListOf<String>()

        val outcome = json.keySet().first { it != UI_DATA }
        params.add(outcome)

        val secondLevel = json.getJSONObject(outcome)
        val enc = secondLevel.keySet().first()
        params.add(enc)


        val thirdLevel = secondLevel.getJSONObject(enc)
        val bucket = thirdLevel.keySet().first()
        params.add(bucket)

        val fourthLevel = thirdLevel.getJSONObject(bucket)
        val learner = fourthLevel.keySet().first()
        params.add(learner)

        val paramArray = fourthLevel.getJSONObject(learner).toMap()
        val properties = mutableSetOf<Property>()
        var collected = false
        paramArray.entries.forEach {
            try {
                if (!collected) {
                    it.key.toInt()
                    // Parse successful - prefix length
                    (it.value as HashMap<String, Any>).entries.forEach {
                        properties.add(Property(it.key, "", it.value.toString(), -1.0, -1.0))
                    }
                    collected = true
                }
            } catch (e: NumberFormatException) {
                properties.add(Property(it.key, "", it.value.toString(), -1.0, -1.0))
            }
        }

        val modelProperties = getModelParams(params)
        modelProperties.first { it.type == "learner" }.properties = properties.toMutableList()

        map[key] = modelProperties

        parseJson(jsonItems, map)
    }
}

private fun getModelParams(paramNames: List<String>): List<ModelParameter> {
    val alreadyDefined = ModelParamProvider().properties.flatMap { it.value }

    val rightParameters = mutableListOf<ModelParameter>()
    paramNames.forEach { param ->
        rightParameters.add(ModelParameter(
                alreadyDefined.firstOrNull { it.parameter == param }
                        ?: ModelParameter(param, param, PREDICTION, true, mutableListOf())))
    }

    return rightParameters
}
