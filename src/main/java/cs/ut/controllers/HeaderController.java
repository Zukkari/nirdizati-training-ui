package cs.ut.controllers;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.HeaderItem;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;

import java.util.Comparator;
import java.util.List;

/**
 * Controller class that is responsible for controls in the header of the page
 */

public class HeaderController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(HeaderController.class);

    @Wire
    Navbar navbar;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        composeHeader();
    }

    /**
     * Constructs header items based on parameters defined in configuration.xml
     */
    private void composeHeader() {
        List<HeaderItem> items = MasterConfiguration.getInstance().getHeaderItems();
        log.debug(String.format("Generating header based on %s items read from configuration", items.size()));
        items.sort(Comparator.comparing(HeaderItem::getPosition));

        items.forEach(it -> {
            Navitem navitem = new Navitem();
            navitem.setLabel(Labels.getLabel(it.getLabel()));
            navitem.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    MainPageController.getInstance().setContent(it.getRedirect(), getPage());
                    navbar.selectItem(navitem);
                }
            });

            log.debug(String.format("Nav item with label '%s' is enabled: %s", it.getLabel(), it.getEnabled()));
            navitem.setDisabled(!it.getEnabled());

            navbar.appendChild(navitem);
        });
    }


    @Listen("onClick = #headerLogo")
    public void handleClick() {
        MainPageController.getInstance().setContent("landing", getPage());
        navbar.setSelectedItem(null);
    }
}
