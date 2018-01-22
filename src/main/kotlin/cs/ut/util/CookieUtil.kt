package cs.ut.util

import cs.ut.engine.IdProvider
import cs.ut.engine.JobManager
import cs.ut.jobs.Job
import org.apache.log4j.Logger
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CookieUtil {
    val log = Logger.getLogger(CookieUtil::class.java)!!


    fun setUpCookie(response: HttpServletResponse) {
        log.debug("Setting up new cookie")
        val cookie = Cookie(JOBS_KEY, IdProvider.getNextId())
        response.addCookie(cookie)
        log.debug("Successfully generated new cookie and added it to response")
    }

    fun getCookieKey(request: HttpServletRequest): String =
        request.cookies.firstOrNull { it.name == JOBS_KEY }?.value ?: ""


    fun getJobsByCookie(request: HttpServletRequest): List<Job> {
        val key: String = getCookieKey(request)
        log.debug("Looking for jobs with cookie key: $key")
        return JobManager.getJobsForKey(key)?.toList() ?: listOf()
    }
}