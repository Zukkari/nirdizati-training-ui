package cs.ut.ui.controllers

import cs.ut.util.Page
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Button

/**
 *  Controller that responds for landing page and controls found on that page
 */
class LandingPageController : SelectorComposer<Component>(), Redirectable {

    @Wire
    private lateinit var upload: Button

    @Wire
    private lateinit var existingLog: Button

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)
        wireButtons()
    }

    /**
     * Sets up listeners for wired buttons.
     * In this case it buttons set content of the page based on uri-s defined in configuration.xml
     */
    private fun wireButtons() {
        upload.addEventListener(Events.ON_CLICK, { _ ->
            setContent(Page.UPLOAD.value, page)
        })

        existingLog.addEventListener(Events.ON_CLICK, { _ ->
            setContent(Page.TRAINING.value, page)
        })
    }
}