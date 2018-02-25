package cs.ut.ui.controllers

import cs.ut.configuration.ConfigurationReader
import cs.ut.configuration.Value
import cs.ut.util.DEST
import cs.ut.util.NAVBAR
import cs.ut.util.PAGE_MODELS_OVERVIEW
import cs.ut.util.PAGE_VALIDATION
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Page
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zkmax.zul.Navbar
import org.zkoss.zkmax.zul.Navitem
import org.zkoss.zul.Include
import java.util.Timer
import kotlin.concurrent.timerTask

interface Redirectable {
    /**
     * Sets content of the page. Since application content is located in a single component, then we change is
     * asynchronously. This is done using this method.
     *
     * @param dest - id of the page to which the content should be changed (defined in configuration.xml)
     * @param page        - caller page where Include element should be looked for.
     */
    fun setContent(dest: String, page: Page) {
        page.title = "${Labels.getLabel("header.$dest")} - Nirdizati"
        val include = Selectors.iterable(page, "#contentInclude").iterator().next() as Include
        include.src = null
        include.src = pages.first { it.identifier == dest }.value
        activateHeaderButton(if (dest == PAGE_VALIDATION) PAGE_MODELS_OVERVIEW else dest, page)
    }

    /**
     * Update content of the page with a delay
     *
     * @param dest new destination
     * @param page page where to update the destination
     * @param delay delay in MS
     * @param desktop client to update the content for
     */
    fun setContent(dest: String, page: Page, delay: Int, desktop: Desktop) {
        Timer().schedule(timerTask {
            Executions.schedule(
                desktop,
                { _ ->
                    setContent(dest, page)
                },
                Event("content change")
            )
        }, delay.toLong())
    }

    /**
     * Active selected header button
     *
     * @param dest new selected value
     * @param page where to update the header
     */
    private fun activateHeaderButton(dest: String, page: Page) {
        val navbar = page.desktop.components.first { it.id == NAVBAR } as Navbar
        val navItem = page.desktop.components.firstOrNull { it.getAttribute(DEST) == dest } as Navitem?
        navItem?.let { navbar.selectItem(navItem) }
    }

    companion object {
        private val pages: List<Value> = ConfigurationReader.findNode("pages")!!.itemList()
    }
}