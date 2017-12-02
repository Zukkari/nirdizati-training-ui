package cs.ut.ui

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

const val ACTUAL = "actual"
const val PREDICTED = "predicted"
const val delim = ","

class ScatterData(val x: Float, val y: Float)

fun getScatterPayload(file: File): String {
    val dataSet = mutableListOf<ScatterData>()
    val gson = Gson()

    var rows = BufferedReader(FileReader(file)).use { it.readLines() }
    val headerNames = rows.toMutableList().removeAt(0).split(delim)
    val indexes = Pair(headerNames.indexOf(ACTUAL), headerNames.indexOf(PREDICTED))
    rows = rows.subList(1, rows.size)

    rows.forEach {
        val items = it.split(delim)
        dataSet.add(ScatterData(x = items.get(indexes.first).toFloat(), y = items.get(indexes.second).toFloat()))
    }
    return gson.toJson(dataSet)
}

fun getLineChartPayload(file: File): String = ""

fun getHistogrammPayload(file: File): String = ""