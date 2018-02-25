package cs.ut.ui

import cs.ut.util.CookieUtil
import org.zkoss.zk.ui.Executions
import javax.servlet.http.HttpServletRequest

/**
 * Interface to ease logging
 * Provides easy access to session id inside controllers
 */
interface UIComponent {

    fun getSessionId(): String = CookieUtil.getCookieKey(Executions.getCurrent().nativeRequest)
}