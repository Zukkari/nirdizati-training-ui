package cs.ut.controllers

import cs.ut.config.ClientInfo
import cs.ut.config.MasterConfiguration
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Page
import org.zkoss.zk.ui.Session
import org.zkoss.zk.ui.event.ClientInfoEvent
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Borderlayout
import org.zkoss.zul.East
import org.zkoss.zul.Include
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class MainPageController : SelectorComposer<Component>() {
    val log: Logger = Logger.getLogger(MainPageController::class.java)!!
    private var clientInformation: Map<Session, ClientInfo> = mapOf()
    private val timer: Timer = Timer()

    @Wire
    private lateinit var mainLayout: Borderlayout

    @Wire
    private lateinit var trackerEast: East

    companion object {
        lateinit var mainPageController: MainPageController
    }

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)
        mainPageController = this
    }

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
        trackerEast.size = "${this.windowWidth * 0.25}px"
        trackerEast.isVisible = false
    }

    /**
     * Retreives browser information for current session
     */
    fun getClientInfo(session: Session): ClientInfo = clientInformation[session] ?: throw NoSuchElementException()


    /**
     * Sets content of the page. Since application content is located in a single component, then we change is
     * asynchronously. This is done using this method.
     *
     * @param destination - id of the page to which the content should be changed (defined in configuration.xml)
     * @param page        - caller page where Include element should be looked for.
     */
    fun setContent(dest: String, page: Page) {
        page.title = Labels.getLabel("header.$dest") + "- Nirdizati"
        val include = Selectors.iterable(page, "#contentInclude").iterator().next() as Include
        include.src = null
        include.src = MasterConfiguration.getInstance().pageConfiguration.getByPageName(dest).uri
    }

    fun setContent(dest: String, page: Page, delay: Int, desktop: Desktop) {
        timer.schedule(timerTask {
            Executions.schedule(desktop,
                    { _ ->
                        setContent(dest, page)
                    },
                    Event("content change"))
        }, delay.toLong())
    }

    fun getComp(): Component = self
}