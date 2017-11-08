package cs.ut.engine

import cs.ut.config.MasterConfiguration
import cs.ut.engine.item.Case
import cs.ut.exceptions.NirdizatiRuntimeException
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Double.max
import java.util.*
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

    companion object Constants {
        const val CASE_ID_COL = "case_id_col"
        const val ACTIVITY_COL = "activity_col"
        const val TIMESTAMP_COL = "timestamp_col"
        const val LABEL_NUM_COLS = "label_num_cols"
        const val LABEL_CAT_COLS = "label_cat_cols"

        const val STATIC = "static"
        const val DYNAMIC = "dynamic"
        const val NUM_COL = "_num_cols"
        const val CAT_COLS = "_cat_cols"

        const val REMTIME = "remtime"
        const val OUTCOME = "label"
    }

    init {
        log.debug("Initializing csv reader...")

        val config = MasterConfiguration.getInstance().csvConfiguration
        splitter = config.splitter.toRegex()
        emptyValues = config.emptyValues
        confThreshold = config.threshold
        sampleSize = config.sampleSize
        activityId = config.activityId
        caseId = config.caseId
        dateFormats = config.timestampFormat.map { it.toRegex() }

        log.debug("Finished initializing csv reader...")
    }

    fun readTableHeader(): List<String> {
        log.debug("Reading table header...")
        BufferedReader(FileReader(f)).use { return it.readLine().split(splitter) }
    }

    fun identifyUserColumns(cols: List<String>): Map<String, String> {
        val result = HashMap<String, String>()

        cols.forEach {
            caseId.forEach { col ->
                val elem = cols.firstOrNull { it.toLowerCase() == col.toLowerCase() }
                elem?.let { result[CASE_ID_COL] = elem }
            }

            activityId.forEach { col ->
                val elem = cols.firstOrNull { it.toLowerCase() == col.toLowerCase() }
                elem?.let { result[ACTIVITY_COL] = elem }
            }
        }
        return result
    }

    fun generateDatasetParams(userCols: Map<String, MutableList<String>>): Map<String, List<String>> {
        val start = System.currentTimeMillis()
        val cases = parseCsv(userCols[CASE_ID_COL]!![0])

        val colValues = HashMap<String, MutableSet<String>>()
        val timestampCol = identifyTimestampColumn(cases.first().attributes) ?: throw NirdizatiRuntimeException("No date column found")

        cases.forEach {
            it.attributes.remove(timestampCol)
            it.attributes.remove(userCols[CASE_ID_COL]!!.first())
            it.attributes.remove(userCols[ACTIVITY_COL]!!.first())

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

        cases.forEach {
            val map = it.classifiedColumns
            it.dynamicCols.forEach { insertIntoMap(map, DYNAMIC, it, colValues[it]) }
            it.staticCols.forEach { insertIntoMap(map, STATIC, it, colValues[it]) }
            postProcessCase(resultCols, it, alreadyClassified)
        }

        resultCols.putAll(userCols)
        resultCols[DYNAMIC + CAT_COLS]!!.add(userCols[ACTIVITY_COL]!!.first())
        resultCols[TIMESTAMP_COL] = mutableListOf(timestampCol)

        val end = System.currentTimeMillis()
        log.debug("Finished generating data set params in ${end - start} ms")

        return resultCols
    }

    private fun postProcessCase(resultCols: MutableMap<String, MutableList<String>>, case: Case, alreadyClassified: MutableSet<String>) {
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
            categorizeColumn(it, DYNAMIC + CAT_COLS, resultCols, alreadyClassified,
                    listOf(STATIC + NUM_COL, STATIC + CAT_COLS, DYNAMIC + NUM_COL))
        }
    }

    private fun categorizeColumn(column: String, key: String, resultCols: MutableMap<String, MutableList<String>>, alreadyClassified: MutableSet<String>, lookthrough: List<String>) {
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

    private fun insertIntoMap(map: MutableMap<String, MutableSet<String>>, category: String, col: String, values: Set<String>?) {
        var isNumeric = true

        values!!.forEach {
            try {
                it.toDouble()
            } catch (e: NumberFormatException) {
                isNumeric = false
                return
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

    fun getColumnList(): MutableList<String> = mutableListOf(STATIC + NUM_COL, STATIC + CAT_COLS, DYNAMIC + NUM_COL, DYNAMIC + CAT_COLS)
}