package cs.ut.util

import cs.ut.configuration.ConfigurationReader
import cs.ut.logging.NirdizatiLogger
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.io.Reader

interface UploadItem {

    val bufferSize: Int
        get() = ConfigurationReader.findNode("fileUpload").valueWithIdentifier("uploadBufferSize").intValue()

    fun write(file: File)

}

class NirdizatiReader(private val reader: Reader) : UploadItem {

    override fun write(file: File) {
        val writer = FileWriter(file)
        var total = 0

        val buffer = CharArray(bufferSize)

        var read = reader.read(buffer)
        while (read != -1) {
            writer.write(buffer)

            total += read
            read = reader.read(buffer)
        }

        log.debug("Read total of $total bytes for file ${file.name}")
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(NirdizatiReader::class.java)
    }
}

class NirdizatiInputStream(private val inputStream: InputStream) : UploadItem {

    override fun write(file: File) {
        val buffer = ByteArray(bufferSize)
        var total = 0

        var read = inputStream.read(buffer)
        FileOutputStream(file).use {
            while (read != -1) {
                it.write(buffer)

                total += read
                read = inputStream.read(buffer)
            }
        }

        log.debug("Read total of $total bytes for file ${file.name}")
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(NirdizatiInputStream::class.java)
    }
}