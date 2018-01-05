package cs.ut.business.engine

import org.apache.commons.codec.binary.Hex
import org.apache.log4j.Logger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*

/**
 * Has to be singleton in order to be thread safe. If used from different threads there is a danger of collision
 * if id is going to be generated at the same time (which is unlikely but possible)
 */
object IdProvider {
    private val log: Logger = Logger.getLogger(IdProvider::class.java)!!

    private val digest = MessageDigest.getInstance("MD5")!!
    private var previous: String = ""

    /**
     * Generate unique identifier for the job
     */
    fun getNextId(): String {
        log.debug("New id requested -> generating id using ${digest.algorithm}")
        var iteration = 1

        var new: String = getNextHash()

        synchronized(this) {
            while (previous == new) {
                new = getNextHash()
                iteration += 1
            }

            log.debug("Finished id generation in $iteration iterations")
            previous = new
        }

        log.debug("New id is -> $new")
        return new

    }

    private fun getNextHash(): String {
        val time = Calendar.getInstance().time.toInstant().toString()
        return String(Hex.encodeHex(digest.digest(time.toByteArray(Charset.forName("UTF-8")))))
    }
}