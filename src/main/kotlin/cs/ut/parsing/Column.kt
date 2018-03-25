package cs.ut.parsing

import cs.ut.configuration.ConfigurationReader
import cs.ut.util.Columns
import java.lang.Double.max


data class Column(val name: String, var values: Set<String>) {

    private fun isNumeric(): Boolean = values.size > threshold && values.all { it.numeric() }

    fun category(): Columns {
        val numeric = isNumeric()
        val isCaseAttribute = values.size == 1

        if (numeric && isCaseAttribute) {
            return Columns.STATIC_NUM_COLS
        }

        if (numeric) {
            return Columns.DYNAMIC_NUM_COLS
        }

        if (isCaseAttribute) {
            return Columns.DYNAMIC_CAT_COLS
        }

        return Columns.STATIC_CAT_COLS
    }

    private fun String.numeric(): Boolean = this.toFloatOrNull() != null
    
    companion object {
        private val configNode = ConfigurationReader.findNode("csv")
        private val confThreshold = configNode.valueWithIdentifier("threshold").intValue()
        private val sampleSize = configNode.valueWithIdentifier("sampleSize").intValue()

        private val threshold = max(confThreshold.toDouble(), 0.001 * sampleSize)
    }
}