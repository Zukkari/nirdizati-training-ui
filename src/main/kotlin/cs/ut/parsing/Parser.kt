package cs.ut.parsing

import cs.ut.configuration.ConfigurationReader
import cs.ut.util.Columns
import cs.ut.util.IdentColumns
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.EnumMap

typealias Row = String

class Case(val id: String) {
    var values: List<Column> = listOf()
}

class CsvParser(val file: File) {
    private lateinit var identified: Deferred<Map<String, String>>

    private var cases: Map<String, Case> = mapOf()
    private val config = ConfigurationReader.findNode("csv")

    private lateinit var header: List<String>
    private val splitter: Regex
    private val sampleSize: Int

    private var columnIndices: Map<IdentColumns, Int> = EnumMap(IdentColumns::class.java)

    init {
        splitter = config.valueWithIdentifier("splitter").value.toRegex()
        sampleSize = config.valueWithIdentifier("sampleSize").intValue()
    }

    private fun getResource(): BufferedReader = BufferedReader(FileReader(file))

    fun getFileHeader(): List<String> {
        val resource = getResource()

        var h = ""
        var v = ""
        resource.use {
            h = it.readLine()
            v = it.readLine()
        }

        header = escape(h).split(splitter)
        identified = async {
            parseUserColumns(v)
        }

        return header
    }

    fun parse(confirmed: Map<IdentColumns, String>): Map<String, List<String>> {
        confirmed.forEach { columnIndices = columnIndices.plus(it.key to header.indexOf(it.value)) }

        getResource()
                .lineSequence()
                .takeWhile { cases.keys.size < sampleSize }
                .forEach { escape(it).processRow() }

        return mapOf()
    }

    private fun String.processRow() {

    }

    private fun parseUserColumns(row: Row): Map<String, String> {
        var res = mapOf<String, String>()

        val rowItems = escape(row).split(splitter)

        val items = config.childNodes.first { it.identifier == "userCols" }.itemList()
        items.forEach { v ->
            val nodeValues = config.childNodes.first { it.identifier == v.value }.itemListValues()
            res += if (v.identifier == IdentColumns.TIMESTAMP.value) {
                val match = rowItems.firstOrNull { item -> nodeValues.any { item.matches(it.toRegex()) } } ?: header[0]
                v.identifier to header[rowItems.indexOf(match)]
            } else {
                val matches = header.filter { nodeValues.contains(it.toLowerCase()) }
                v.identifier to (matches.firstOrNull() ?: header[0])
            }
        }

        return res
    }

    suspend fun getUserColumns(): Map<String, String> = identified.await()

    private fun escape(row: Row): Row {
        val escapeNode = config.childNodes.first { it.identifier == "escape" }

        return row.replace(escapeNode.valueWithIdentifier("regex").value.toRegex()
                , transform = {
            var item = it.value
            escapeNode.childNodes.first { it.identifier == "replacement" }.values.forEach {
                item = item.replace(it.identifier, it.value)
            }
            item
        })
    }
}