package cs.ut.ui.controllers

import cs.ut.configuration.ConfigNode
import cs.ut.configuration.ConfigurationReader
import cs.ut.util.DEST
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PAGE_LANDING
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zkmax.zul.Navbar
import org.zkoss.zkmax.zul.Navitem

class HeaderController : SelectorComposer<Component>(), Redirectable {

    private val configNode = ConfigurationReader.findNode("header")!!

    @Wire
    private lateinit var navbar: Navbar

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)
        composeHeader()
    }

    private fun composeHeader() {
        val items: List<ConfigNode> = configNode.childNodes

        items.forEach {
            val navItem = Navitem()
            navItem.label = NirdizatiUtil.localizeText(it.valueWithIdentifier("label").value)
            navItem.setAttribute(DEST, it.valueWithIdentifier("redirect").value)
            navItem.iconSclass = it.valueWithIdentifier("icon").value
            navItem.sclass = "n-nav-item"
            navItem.addEventListener(Events.ON_CLICK, { _ ->
                setContent(it.valueWithIdentifier("redirect").value, page)
                navbar.selectItem(navItem)
            })
            navItem.isVisible = it.isEnabled()

            navbar.appendChild(navItem)
        }
    }

    @Listen("onClick = #headerLogo")
    fun handleClick() {
        setContent(PAGE_LANDING, page)
        navbar.selectItem(null)
    }
}