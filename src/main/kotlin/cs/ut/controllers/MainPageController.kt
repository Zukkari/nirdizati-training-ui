package cs.ut.controllers

import cs.ut.config.ClientInfo
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Session
import org.zkoss.zk.ui.event.ClientInfoEvent
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Borderlayout
import org.zkoss.zul.East
import java.util.*

class MainPageController : SelectorComposer<Component>(), Redirectable {
    val log: Logger = Logger.getLogger(MainPageController::class.java)!!
    private var clientInformation: Map<Session, ClientInfo> = mapOf()
    private val timer: Timer = Timer()

    @Wire
    private lateinit var mainLayout: Borderlayout

    @Wire
    private lateinit var trackerEast: East

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

        Executions.getCurrent().desktop.enableServerPush(true)
        clientInformation += mapOf(Executions.getCurrent().session to info)
        info.configureTracker()
        log.debug("Finished gathering information about browser")
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