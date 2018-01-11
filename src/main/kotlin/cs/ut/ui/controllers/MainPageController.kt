package cs.ut.ui.controllers

import cs.ut.config.ClientInfo
import cs.ut.ui.controllers.JobTrackerController.Companion.GRID_ID
import cs.ut.engine.JobManager
import cs.ut.jobs.Job
import cs.ut.ui.NirdizatiGrid
import cs.ut.util.CookieUtil
import cs.ut.util.NAVBAR
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Session
import org.zkoss.zk.ui.event.ClientInfoEvent
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zkmax.zul.Navbar
import org.zkoss.zul.Borderlayout
import org.zkoss.zul.East
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainPageController : SelectorComposer<Component>(), Redirectable {
    val log: Logger = Logger.getLogger(MainPageController::class.java)!!
    private var clientInformation: Map<Session, ClientInfo> = mapOf()

    @Wire
    private lateinit var mainLayout: Borderlayout

    @Wire
    private lateinit var trackerEast: East

    private val cookieUtil = CookieUtil()

    @Listen("onClientInfo = #mainLayout")
    fun gatherInformation(e: ClientInfoEvent) {
        log.debug("Client info event, gathering browser information")
        val info = ClientInfo(
                e.screenWidth,
                e.screenHeight,
                e.desktopWidth,
                e.desktopHeight,
                e.colorDepth,
                e.orientation
        )

        if (e.desktopWidth <= 680) {
            updateHeader(true)
        } else {
            updateHeader()
        }

        Executions.getCurrent().desktop.enableServerPush(true)
        clientInformation += mapOf(Executions.getCurrent().session to info)
        info.configureTracker()
        log.debug("Finished gathering information about browser")

        handleCookie()
    }

    private fun updateHeader(collapse: Boolean = false) {
        Executions.getCurrent().desktop.components.firstOrNull { it.id == NAVBAR }?.let {
            it as Navbar
            it.isCollapsed = collapse
        }
    }

    /**
     * Handles users cookie so jobs could be persistent if user refreshes the page.
     */
    private fun handleCookie() {
        val request = Executions.getCurrent().nativeRequest as HttpServletRequest
        val cookieKey: String = cookieUtil.getCookieKey(request)
        if (cookieKey.isBlank()) {
            cookieUtil.setUpCookie(Executions.getCurrent().nativeResponse as HttpServletResponse)
        } else {
            val jobGrid: NirdizatiGrid<Job> = Executions.getCurrent().desktop.components.first { it.id == GRID_ID } as NirdizatiGrid<Job>
            val jobs: List<Job> = cookieUtil.getJobsByCookie(request)
            if (jobs.isNotEmpty()) {
                jobGrid.generate(jobs)
                trackerEast.isVisible = true
            }
        }
    }

    /**
     * Configures tracker to be suitable for browsers screen size
     */
    private fun ClientInfo.configureTracker() {
        log.debug("Configuring job tracker for $this")
        trackerEast.size = "${this.windowWidth * 0.3}px"
        trackerEast.isVisible = false
    }

    /**
     * Retreives browser information for current session
     */
    fun getClientInfo(session: Session): ClientInfo = clientInformation[session] ?: throw NoSuchElementException()

    fun getComp(): Component = self
}