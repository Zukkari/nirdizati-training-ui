package cs.ut.csv

import cs.ut.configuration.ConfigurationReader

typealias Attributes = MutableList<Attribute>

data class Case(val caseId: String,
                var attributes: Attributes) {
    constructor(caseId: String) : this(caseId, mutableListOf<Attribute>())
}


data class Attribute(val name: String, var values: MutableSet<String> = hashSetOf()) {

    val isEventAttribute by lazy { values.size == 1 }
}

data class ClassificationResult(
        val caseId: String,
        val activity: String,
        val timestamp: String,
        val resource: String
) {
    constructor() : this("", "", "", "")
}


enum class ColumnValue(val value: String) {
    CASE_ID("case_id"),
    ACTIVITY("activity"),
    TIMESTAMP("timestamp_col")
}