package cs.ut.controllers

import cs.ut.config.MasterConfiguration
import cs.ut.util.DEST
import cs.ut.util.NAVBAR
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Page
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zkmax.zul.Navbar
import org.zkoss.zkmax.zul.Navitem
import org.zkoss.zul.Include
import java.util.*
import kotlin.concurrent.timerTask

interface Redirectable {
    /**
     * Sets content of the page. Since application content is located in a single component, then we change is
     * asynchronously. This is done using this method.
     *
     * @param destination - id of the page to which the content should be changed (defined in configuration.xml)
     * @param page        - caller page where Include element should be looked for.
     */
    fun setContent(dest: String, page: Page) {
        page.title = "${Labels.getLabel("header.$dest")} - Nirdizati"
        val include = Selectors.iterable(page, "#contentInclude").iterator().next() as Include
        include.src = null
        include.src = MasterConfiguration.pageConfiguration.getPageByName(dest).uri
        activateHeaderButton(dest, page)
    }

    fun setContent(dest: String, page: Page, delay: Int, desktop: Desktop) {
        Timer().schedule(timerTask {
            Executions.schedule(desktop,
                    { _ ->
                        setContent(dest, page)
                    },
                    Event("content change"))
        }, delay.toLong())
    }

    private fun activateHeaderButton(dest: String, page: Page) {
        val navbar = page.desktop.components.first { it.id == NAVBAR } as Navbar
        val navItem = page.desktop.components.firstOrNull { it.getAttribute(DEST) == dest } as Navitem?
        navItem?.let { navbar.selectItem(navItem) }
    }
}