package cs.ut.controllers

import cs.ut.config.MasterConfiguration
import cs.ut.config.items.HeaderItem
import cs.ut.util.DEST
import cs.ut.util.PAGE_LANDING
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zkmax.zul.Navbar
import org.zkoss.zkmax.zul.Navitem

class HeaderController : SelectorComposer<Component>(), Redirectable {

    @Wire
    private lateinit var navbar: Navbar

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)
        composeHeader()
    }

    private fun composeHeader() {
        val items: List<HeaderItem> = MasterConfiguration.headerConfiguration.headerItems
        items.sortedBy { it.position }

        items.forEach {
            val navItem = Navitem()
            navItem.label = Labels.getLabel(it.label)
            navItem.setAttribute(DEST, it.redirect)
            navItem.iconSclass = it.icon
            navItem.sclass = "n-nav-item"
            navItem.addEventListener(Events.ON_CLICK, { _ ->
                setContent(it.redirect, page)
                navbar.selectItem(navItem)
            })
            navItem.isDisabled = !it.enabled

            navbar.appendChild(navItem)
        }
    }

    @Listen("onClick = #headerLogo")
    fun handleClick() {
        setContent(PAGE_LANDING, page)
        navbar.selectItem(null)
    }
}