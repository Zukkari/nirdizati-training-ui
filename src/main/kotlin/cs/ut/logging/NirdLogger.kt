package cs.ut.logging

import cs.ut.util.CookieUtil
import org.apache.log4j.Logger
import javax.servlet.http.HttpServletRequest

class NirdLogger(private val id: String = "GLOBAL", caller: Class<Any>) {

    val log = Logger.getLogger(caller::class.java)!!

    fun debug(msg: Any?) {
        log.debug("[${this.id}] $msg")
    }

    fun debug(msg: Any?, e: Exception) {
        log.debug("[${this.id}] $msg", e)
    }

    fun error(msg: Any?, e: Exception) {
        log.error("[${this.id}] $msg", e)
    }

    fun error(msg: Any?) {
        log.error("[${this.id}] $msg")
    }

    companion object {
        fun getId(request: Any): String =
            CookieUtil().getCookieKey(request as HttpServletRequest)
    }
}