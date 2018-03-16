package cs.ut.util

import cs.ut.logging.NirdizatiLogger
import java.io.InputStream
import java.io.Reader

interface UploadItem {

    fun read(byteArray: ByteArray): Int

}

class NirdizatiReader(private val reader: Reader) : UploadItem {

    override fun read(byteArray: ByteArray): Int {
        val innerBuff = CharArray(byteArray.size)

        val read = reader.read(innerBuff)
        log.debug("Read chunk of $read bytes")

        for ((i, char) in innerBuff.withIndex()) {
            byteArray[i] = char.toByte()
        }

        return read
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(NirdizatiReader::class.java)
    }
}

class NirdizatiInputStream(private val inputStream: InputStream) : UploadItem {

    override fun read(byteArray: ByteArray): Int {
        val read = inputStream.read(byteArray)
        log.debug("Read chunk of $read bytes")
        return read
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(NirdizatiInputStream::class.java)
    }
}