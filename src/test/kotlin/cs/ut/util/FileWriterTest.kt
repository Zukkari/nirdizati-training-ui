package cs.ut.util

import cs.ut.config.MasterConfiguration
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileReader
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileWriterTest {
    companion object {
        var writer by Delegates.notNull<FileWriter>()
        var jsonName = "testJson"

        val dir = MasterConfiguration.directoryPathConfiguration
                .scriptDirectory + "core/"

        @BeforeClass
        @JvmStatic
        fun setUp() {
            writer = FileWriter()
        }

        @AfterClass
        @JvmStatic
        fun cleanUp() {
            assert(File(dir + jsonName + ".json").delete())
        }
    }

    @Test
    fun writeJsonTest() {
        val json = JSONObject()
        json.put("test", "data")

        writer.writeJsonToDisk(json, jsonName, "")

        val file = File(dir + jsonName + ".json")
        assertTrue(file.exists())

        val reader = FileReader(file)

        val jsonData = reader.readLines();
        assertEquals(json.toString(), jsonData[0])
    }
}