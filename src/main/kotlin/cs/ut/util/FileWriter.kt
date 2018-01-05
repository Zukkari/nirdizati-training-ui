package cs.ut.util

import cs.ut.config.MasterConfiguration
import cs.ut.exceptions.NirdizatiRuntimeException
import org.apache.log4j.Logger
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class FileWriter {

    private val log = Logger.getLogger(FileWriter::class.java)

    private val pathProvider = MasterConfiguration.directoryPathConfiguration
    private val scriptDir = pathProvider.scriptDirectory
    private val coreDir = scriptDir + "core/"

    fun writeJsonToDisk(json : JSONObject, fileName : String, path : String): File {
        log.debug("Writing json <$json> to disk...")

        val file = File(coreDir + path + fileName + ".json")

        val bytes = json.toString().toByteArray(Charset.forName("UTF-8"))

        if (!file.exists()) {
            try {
                Files.createFile(Paths.get(file.absolutePath))
                log.debug("Successfully create file $file")
            } catch (e : IOException) {
                throw NirdizatiRuntimeException("Failed to create file ${file.absolutePath}")
            }
        }

        FileOutputStream(file).use { it.write(bytes) }
        log.debug("Successfully written json to disk. <${bytes.size}> bytes were written")

        return file
    }
}