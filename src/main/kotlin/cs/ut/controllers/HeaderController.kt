package cs.ut.controllers

import cs.ut.config.MasterConfiguration
import cs.ut.util.PAGE_LANDING
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zkmax.zul.Navbar
import org.zkoss.zkmax.zul.Navitem

class HeaderController : SelectorComposer<Component>() {

    @Wire
    private lateinit var navbar: Navbar

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)
        composeHeader()
    }

    private fun composeHeader() {
        val items = MasterConfiguration.getInstance().headerItems
        items.sortBy { it.position }

        items.forEach {
            val navItem = Navitem()
            navItem.label = Labels.getLabel(it.label)
            navItem.addEventListener(Events.ON_CLICK, { _ ->
                MainPageController.getInstance().setContent(it.redirect, page)
                navbar.selectItem(navItem)
            })
            navItem.isDisabled = !it.enabled
            navItem.style = "padding-left: 15px"

            navbar.appendChild(navItem)
        }
    }

    @Listen("onClick = #headerLogo")
    fun handleClick() {
        MainPageController.getInstance().setContent(PAGE_LANDING, page)
        navbar.selectItem(null)
    }
}