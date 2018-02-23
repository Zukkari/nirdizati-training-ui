package cs.ut.util

import cs.ut.configuration.ConfigurationReader
import cs.ut.engine.item.Case
import cs.ut.exceptions.NirdizatiRuntimeException
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Double.max
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap
import kotlin.properties.Delegates

class CsvReader(private val f: File) {
    private val log = Logger.getLogger(CsvReader::class.java)!!

    private var splitter by Delegates.notNull<Regex>()
    private var emptyValues by Delegates.notNull<List<String>>()
    private var confThreshold by Delegates.notNull<Int>()
    private var sampleSize by Delegates.notNull<Int>()
    private var caseId by Delegates.notNull<List<String>>()
    private var activityId by Delegates.notNull<List<String>>()
    private var dateFormats by Delegates.notNull<List<Regex>>()
    private var resourceId by Delegates.notNull<List<String>>()

    init {
        log.debug("Initializing csv reader...")

        val configNode = ConfigurationReader.findNode("csv")!!
        splitter = configNode.valueWithIdentifier("splitter").value.toRegex()
        confThreshold = configNode.valueWithIdentifier("threshold").intValue()
        sampleSize = configNode.valueWithIdentifier("sampleSize").intValue()

        emptyValues = ConfigurationReader.findNode("csv/empty")!!.itemListValues()
        activityId = ConfigurationReader.findNode("csv/activityId")!!.itemListValues()
        caseId = ConfigurationReader.findNode("csv/caseId")!!.itemListValues()
        dateFormats = ConfigurationReader.findNode("csv/timestamp")!!.itemListValues().map { it.toRegex() }
        resourceId = ConfigurationReader.findNode("csv/resource")!!.itemListValues()

        log.debug("Finished initializing csv reader...")
    }

    fun readTableHeader(): List<String> {
        log.debug("Reading table header...")
        BufferedReader(FileReader(f)).use { return it.readLine().split(splitter) }
    }

    tailrec fun identifyUserColumns(cols: List<String>, result: MutableMap<String, String>) {
        if (cols.isNotEmpty()) {
            val head = cols.first()

            identifyColumn(head, caseId.toMutableList(), CASE_ID_COL, result)
            identifyColumn(head, activityId.toMutableList(), ACTIVITY_COL, result)
            identifyColumn(head, resourceId.toMutableList(), RESOURCE_COL, result)
            identifyUserColumns(cols.drop(1), result)
        }
    }

    private tailrec fun identifyColumn(
        col: String,
        ids: MutableList<String>,
        type: String,
        result: MutableMap<String, String>
    ) {
        if (ids.isNotEmpty()) {
            if (col.toLowerCase() in ids.first()) {
                result[type] = col
            } else {
                identifyColumn(col, ids.drop(1).toMutableList(), type, result)
            }
        }
    }

    fun generateDataSetParams(userCols: MutableMap<String, Any>): MutableMap<String, MutableList<String>> {
        val start = System.currentTimeMillis()
        val case = userCols[CASE_ID_COL] ?: throw NirdizatiRuntimeException("No case id column in log")
        val cases = parseCsv(case as String)

        val colValues = HashMap<String, MutableSet<String>>()

        cases.forEach {
            it.attributes.remove(userCols[TIMESTAMP_COL])
            it.attributes.remove(userCols[CASE_ID_COL])
            it.attributes.remove(userCols[ACTIVITY_COL])
            it.attributes.remove(userCols[RESOURCE_COL])

            if (it.attributes.keys.contains(REMTIME)) {
                it.attributes.remove(REMTIME)
            }

            if (it.attributes.keys.contains(OUTCOME)) {
                it.attributes.remove(OUTCOME)
            }

            classifyColumns(it)

            it.attributes.forEach { k, v ->
                if (colValues.containsKey(k)) colValues[k]!!.addAll(v)
                else {
                    colValues[k] = mutableSetOf()
                    colValues[k]!!.addAll(v)
                }
            }
        }

        val alreadyClassified = mutableSetOf<String>()
        val resultCols = mutableMapOf<String, MutableList<String>>()
        resultCols[STATIC + CAT_COLS] = mutableListOf()
        resultCols[STATIC + NUM_COL] = mutableListOf()
        resultCols[DYNAMIC + CAT_COLS] = mutableListOf()
        resultCols[DYNAMIC + NUM_COL] = mutableListOf()

        cases.forEach { c ->
            c.dynamicCols.forEach { insertIntoMap(c.classifiedColumns, DYNAMIC, it, colValues[it]) }
            c.staticCols.forEach { insertIntoMap(c.classifiedColumns, STATIC, it, colValues[it]) }
            postProcessCase(resultCols, c, alreadyClassified)
        }

        userCols.forEach { k, v -> resultCols[k] = Collections.singletonList(v as String) }
        val end = System.currentTimeMillis()
        log.debug("Finished generating data set params in ${end - start} ms")

        return resultCols
    }

    private fun postProcessCase(
        resultCols: MutableMap<String, MutableList<String>>,
        case: Case,
        alreadyClassified: MutableSet<String>
    ) {
        val caseCols = case.classifiedColumns

        caseCols[STATIC + NUM_COL]?.forEach {
            categorizeColumn(it, STATIC + NUM_COL, resultCols, alreadyClassified, emptyList())
        }

        caseCols[STATIC + CAT_COLS]?.forEach {
            categorizeColumn(it, STATIC + CAT_COLS, resultCols, alreadyClassified, listOf(STATIC + NUM_COL))
        }

        caseCols[DYNAMIC + NUM_COL]?.forEach {
            categorizeColumn(it, DYNAMIC + NUM_COL, resultCols, alreadyClassified, listOf(STATIC + NUM_COL))
        }

        caseCols[DYNAMIC + CAT_COLS]?.forEach {
            categorizeColumn(
                it, DYNAMIC + CAT_COLS, resultCols, alreadyClassified,
                listOf(STATIC + NUM_COL, STATIC + CAT_COLS, DYNAMIC + NUM_COL)
            )
        }
    }

    private fun categorizeColumn(
        column: String,
        key: String,
        resultCols: MutableMap<String, MutableList<String>>,
        alreadyClassified: MutableSet<String>,
        lookthrough: List<String>
    ) {
        if (column !in alreadyClassified) {
            alreadyClassified.add(column)
            resultCols[key]!!.add(column)
        } else {
            lookthrough.forEach {
                if (resultCols[it]!!.contains(column)) {
                    resultCols[it]!!.remove(column)
                    resultCols[key]!!.add(column)
                }
            }
        }
    }

    private fun insertIntoMap(
        map: MutableMap<String, MutableSet<String>>,
        category: String,
        col: String,
        values: Set<String>?
    ) {
        var isNumeric = true

        values!!.forEach {
            try {
                it.toDouble()
            } catch (e: NumberFormatException) {
                isNumeric = false
                return@forEach
            }
        }

        val threshold = max(confThreshold.toDouble(), 0.001 * sampleSize)
        if (values.size < threshold || !isNumeric) {
            if (category + CAT_COLS in map.keys) map[category + CAT_COLS]!!.add(col)
            else map[category + CAT_COLS] = mutableSetOf(col)
        } else {
            if (category + NUM_COL in map.keys) map[category + NUM_COL]!!.add(col)
            else map[category + NUM_COL] = mutableSetOf(col)
        }
    }

    private fun classifyColumns(case: Case) {
        case.attributes.forEach { k, v ->
            emptyValues.forEach {
                if (v.contains(it)) v.remove(it)
            }

            if (v.size == 1) case.staticCols.add(k)
            else case.dynamicCols.add(k)
        }
    }

    private fun identifyTimestampColumn(attributes: LinkedHashMap<String, MutableSet<String>>): String? {
        return attributes.filter { isDateCol(it.value.first()) }.keys.firstOrNull()
    }

    private fun isDateCol(col: String): Boolean {
        dateFormats.forEach {
            if (col.matches(it)) return true
        }
        return false
    }

    private fun parseCsv(caseIdColumn: String): List<Case> {
        log.debug("Started parsing csv")
        val start = System.currentTimeMillis()

        val cases = mutableSetOf<Case>()
        var caseIdColIndex: Int

        var header: List<String>
        BufferedReader(FileReader(f)).use {
            val line = it.readLine()
            if (line.isBlank()) throw NirdizatiRuntimeException("File is empty")
            else {
                header = line.split(splitter)
                caseIdColIndex = header.indexOf(caseIdColumn)
            }

            it.lineSequence()
                .takeWhile { cases.size < sampleSize }
                .forEach { row -> processRow(row, cases, caseIdColIndex, header) }
        }

        val end = System.currentTimeMillis()
        log.debug("Finished parsing csv in ${end - start} ms")
        return cases.toList()
    }

    private fun processRow(row: String, cases: MutableSet<Case>, caseIndex: Int, head: List<String>) {
        val cols = row.split(splitter)

        val case = findCaseById(cols[caseIndex], cases) ?: prepareCase(head, cols[caseIndex])
        val keys = case.attributes.keys.toList()

        for (i in (0 until cols.size)) {
            case.attributes[keys[i]]?.add(cols[i])
        }
        cases.add(case)
    }

    private fun prepareCase(head: List<String>, id: String): Case {
        val c = Case(id)
        head.forEach { c.attributes[it] = mutableSetOf() }
        return c
    }

    private fun findCaseById(colName: String, cases: Set<Case>): Case? {
        return cases.firstOrNull { colName.toLowerCase() == it.id.toLowerCase() }
    }

    fun getColumnList(): MutableList<String> =
        mutableListOf(STATIC + NUM_COL, STATIC + CAT_COLS, DYNAMIC + NUM_COL, DYNAMIC + CAT_COLS)

    private fun readOneCase(): Case {
        val reader = BufferedReader(FileReader(f))
        val heads = reader.readLine().split(splitter)
        val items = reader.readLine().split(splitter)

        val case = Case("")
        heads.zip(items).forEach { case.attributes[it.first] = mutableSetOf(it.second) }

        return case
    }

    fun getTimeStamp(): String = identifyTimestampColumn(readOneCase().attributes) ?: ""
}