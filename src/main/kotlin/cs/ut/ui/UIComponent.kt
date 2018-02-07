package cs.ut.ui

import cs.ut.util.CookieUtil
import org.zkoss.zk.ui.Executions
import javax.servlet.http.HttpServletRequest

interface UIComponent {

    fun getSessionId(): String = CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
}