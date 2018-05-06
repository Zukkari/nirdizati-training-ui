package cs.ut.json

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import cs.ut.engine.item.ModelParameter

enum class JSONKeys(val value: String) {
    UI_DATA("ui_data"),
    EVALUATION("evaluation"),
    OWNER("owner"),
    LOG_FILE("log_file"),
    START("startTime"),
    METRIC("metric"),
    VALUE("value")
}

data class TrainingConfiguration(
        @get:JsonIgnore val encoding: ModelParameter,
        @get:JsonIgnore val bucketing: ModelParameter,
        @get:JsonIgnore val learner: ModelParameter,
        @get:JsonIgnore val outcome: ModelParameter
) {
    lateinit var info: JobInformation
        @JsonIgnore get

    lateinit var evaluation: Report
        @JsonIgnore get

    @JsonAnyGetter
    fun getProperties(): Map<String, Any> {
        fun String.safeConvert() : Number = this.toIntOrNull() ?: this.toFloat()

        val map = mutableMapOf<String, Any>()

        val learnerMap = mutableMapOf<String, Any>()
        learner.properties.forEach { learnerMap[it.id] = it.property.safeConvert() }
        bucketing.properties.forEach { learnerMap[it.id] = it.property.safeConvert() }

        val encodingMap = mapOf<String, Any>(learner.parameter to learnerMap)
        val bucketingMap = mapOf<String, Any>(encoding.parameter to encodingMap)
        val targetMap = mapOf<String, Any>(bucketing.parameter to bucketingMap)

        map[outcome.parameter] = targetMap
        map[JSONKeys.UI_DATA.value] = JSONHandler().toMap(info)

        return map
    }
}

data class JobInformation(
        var owner: String,
        @field:JsonProperty(value = "log_file") var logFile: String,
        var startTime: String
) {
    constructor() : this("", "", "")
}

class Report {
    var metric: String = ""
    var value: Double = 0.0
}

class TrainingData {

    @JsonProperty(value = "static_cat_cols")
    var staticCategorical = listOf<String>()

    @JsonProperty(value = "dynamic_cat_cols")
    var dynamicCategorical = listOf<String>()

    @JsonProperty(value = "static_num_cols")
    var staticNumeric = listOf<String>()

    @JsonProperty(value = "dynamic_num_cols")
    var dynamicNumeric = listOf<String>()

    @JsonProperty(value = "case_id_col")
    var caseId = ""

    @JsonProperty(value = "activity_col")
    var activity = ""

    @JsonProperty(value = "timestamp_col")
    var timestamp = ""

    @JsonProperty(value = "future_values")
    var futureValues = listOf<String>()

    fun getAllColumns(): List<String> = ((staticCategorical + staticNumeric + futureValues).toList() - activity)
}