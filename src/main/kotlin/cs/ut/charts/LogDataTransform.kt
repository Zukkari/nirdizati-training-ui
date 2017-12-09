package cs.ut.charts

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

const val ACTUAL = "actual"
const val PREDICTED = "predicted"
const val NR_EVENTS = "nr_events"
const val SCORE = "score"
const val delim = ","
const val MAE = "mae"

class LinearData(val x: Float, val y: Float)

enum class Mode {
    SCATTER,
    LINE
}

fun getLinearPayload(file: File, mode: Mode): List<LinearData> {
    val dataSet = mutableListOf<LinearData>()

    var indexes = Pair(-1, -1)
    BufferedReader(FileReader(file)).lines().forEach {
        if (indexes.first == -1) {
            val headerItems = it.split(delim)
            indexes = if (Mode.SCATTER == mode) Pair(headerItems.indexOf(ACTUAL), headerItems.indexOf(PREDICTED))
            else Pair(headerItems.indexOf(NR_EVENTS), headerItems.indexOf(SCORE))
        } else if (it.contains(MAE) || mode == Mode.SCATTER) {
            val items = it.split(delim)
            dataSet.add(
                    LinearData(
                            x = items.get(indexes.first).toFloat() / if (mode == Mode.SCATTER) 86400 else 1,
                            y = items.get(indexes.second).toFloat() / 86400))
        }

    }
    return dataSet
}

const val LABEL_INDEX = 0
const val VALUE_INDEX = 1

class BarChartData(val label: String, val value: Float)

fun getBarChartPayload(file: File): List<BarChartData> {
    val dataSet = mutableListOf<BarChartData>()

    var rows = BufferedReader(FileReader(file)).use { it.readLines() }
    rows = rows.subList(1, rows.size)

    rows.forEach {
        val items = it.split(delim)
        dataSet.add(BarChartData(items.get(LABEL_INDEX), items.get(VALUE_INDEX).toFloat()))
    }

    return dataSet
}