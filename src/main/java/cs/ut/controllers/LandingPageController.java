package cs.ut.controllers;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.PageConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;

/**
 *  Controller that responds for landing page and controls found on that page
 */

public class LandingPageController extends SelectorComposer<Component> {

    @Wire
    Button upload;

    @Wire
    Button existingLog;

    private PageConfiguration pageProvider;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        pageProvider = MasterConfiguration.getInstance().getPageConfiguration();

        wireButtons();
    }

    /**
     * Sets up listeners for wired buttons.
     * In this case it buttons set content of the page based on uri-s defined in configuration.xml
     */
    private void wireButtons() {
        upload.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                MainPageController.getInstance().setContent("upload", getPage());
            }
        });

        existingLog.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                MainPageController.getInstance().setContent("training", getPage());
            }
        });
    }
}
