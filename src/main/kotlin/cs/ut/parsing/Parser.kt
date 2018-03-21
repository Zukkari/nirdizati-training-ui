package cs.ut.parsing

import cs.ut.configuration.ConfigurationReader
import cs.ut.util.Columns
import cs.ut.util.IdentColumns
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

typealias Row = String

class Case {
    var values: List<Column> = listOf()

    fun getColumns(enum: Columns): List<Column> = TODO()
}

abstract class Parser(val file: File) {

    abstract fun getFileHeader(): List<String>

    abstract fun parse(splitter: Regex, header: List<String>)

    abstract fun getUserColumns(): Map<String, String>

    abstract fun escape(row: Row): Row

    abstract fun getResource(): BufferedReader
}


class CsvParser(file: File) : Parser(file) {
    private lateinit var parseThread: Deferred<Unit>

    private lateinit var identified: Deferred<Map<String, String>>

    private var cases: Map<String, Case> = mapOf()
    private val config = ConfigurationReader.findNode("csv")

    override fun getResource(): BufferedReader = BufferedReader(FileReader(file))

    override fun getFileHeader(): List<String> {
        val splitter = config.valueWithIdentifier("splitter").value.toRegex()

        val resource = getResource()

        val header: List<String> = escape(resource.readLine()).split(splitter)

        identified = async {
            parseUserColumns(resource.readLine(), header, splitter)
        }

        return header
    }

    override fun parse(splitter: Regex, header: List<String>) {

    }

    private fun parseUserColumns(row: Row, header: List<String>, splitter: Regex): Map<String, String> {
        var res = mapOf<String, String>()

        val rowItems = escape(row).split(splitter)

        val items = config.childNodes.first { it.identifier == "userCols" }.itemList()
        items.forEach { v ->
            val nodeValues = config.childNodes.first { it.identifier == v.value }.itemListValues()
            res += if (v.identifier == IdentColumns.TIMESTAMP.value) {
                val match = rowItems.first { item -> nodeValues.any { item.matches(it.toRegex()) } }
                v.identifier to header[rowItems.indexOf(match)]
            } else {
                val matches = header.filter { nodeValues.contains(it.toLowerCase()) }
                v.identifier to matches.first()
            }
        }

        return res
    }

    override fun getUserColumns(): Map<String, String> = identified.getCompleted()

    override fun escape(row: Row): Row {
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