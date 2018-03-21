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

        val buffer = CharArray(bufferSize)

        while (reader.read(buffer) == bufferSize) {
            writer.write(buffer)
        }
    }
}

class NirdizatiInputStream(private val inputStream: InputStream) : UploadItem {

    override fun write(file: File) {
        val buffer = ByteArray(bufferSize)

        FileOutputStream(file).use {
            while (inputStream.read(buffer) == bufferSize) {
                it.write(buffer)
            }
        }
    }
}