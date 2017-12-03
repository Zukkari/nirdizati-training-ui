package cs.ut.ui

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

    var rows = BufferedReader(FileReader(file)).use { it.readLines() }
    val headerNames = rows.toMutableList().removeAt(0).split(delim)

    val indexes: Pair<Int, Int> =
            if (Mode.SCATTER == mode) Pair(headerNames.indexOf(ACTUAL), headerNames.indexOf(PREDICTED))
            else Pair(headerNames.indexOf(NR_EVENTS), headerNames.indexOf(SCORE))

    rows = rows.subList(1, rows.size)

    rows.filter { it.contains(MAE) || mode == Mode.SCATTER }.forEach {
        val items = it.split(delim)
        dataSet.add(LinearData(x = items.get(indexes.first).toFloat(), y = items.get(indexes.second).toFloat() / 86400))
    }
    return dataSet
}

fun getBarChartPayload(file: File): String = ""