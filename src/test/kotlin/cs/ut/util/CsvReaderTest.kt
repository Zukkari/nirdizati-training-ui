package cs.ut.util

import cs.ut.exceptions.NirdizatiRuntimeException
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileWriter
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CsvReaderTest {
    companion object {
        private var reader by Delegates.notNull<CsvReader>()
        private var testCsv by Delegates.notNull<File>()

        @BeforeClass
        @JvmStatic
        fun setUp() {
            testCsv = File("test.csv")
            val testTable =
                    """a;b;c
                    e;f;1
                """

            val w = FileWriter(testCsv)
            w.write(testTable)
            w.close()

            reader = CsvReader(testCsv)
        }

        @AfterClass
        @JvmStatic
        fun cleanUp() {
            assertTrue(testCsv.delete(), "Failed to delete test file")
        }
    }


    @Test
    fun readTableHeaderTest() {
        val header = reader.readTableHeader()
        assertEquals(header, listOf("a", "b", "c"))
    }

    @Test
    fun identifyUserColsTest01() {
        val resultMap = mutableMapOf<String, String>()

        reader.identifyUserColumns(listOf(), resultMap)
        assertTrue(resultMap.isEmpty())
    }

    @Test
    fun identifyUserColsTest02() {
        val resultMap = mutableMapOf<String, String>()
        val activity = "activity"
        val case = "caseid"

        reader.identifyUserColumns(listOf(activity, case), resultMap)
        assertEquals(activity, resultMap[ACTIVITY_COL])
        assertEquals(case, resultMap[CASE_ID_COL])
    }

    @Test
    fun identifyUserColsTest03() {
        val resultMap = mutableMapOf<String, String>()

        reader.identifyUserColumns(listOf("none", "not", "a", "column"), resultMap)
        assertTrue(resultMap.isEmpty())
    }

    @Test
    fun getColumnsTest() {
        assertEquals(listOf(
                STATIC + NUM_COL,
                STATIC + CAT_COLS,
                DYNAMIC + NUM_COL,
                DYNAMIC + CAT_COLS
        ), reader.getColumnList())
    }

    @Test
    fun generateDatasetParamsTest01() {
        assertFailsWith<NirdizatiRuntimeException> {
            reader.generateDatasetParams(mutableMapOf())
        }
    }

    @Test
    fun generateDatasetParamsTest02() {
        assertFailsWith<NirdizatiRuntimeException> {
            reader.generateDatasetParams(mutableMapOf())
        }
    }
}