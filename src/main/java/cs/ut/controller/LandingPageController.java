package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.PageConfigurationProvider;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;


public class LandingPageController extends SelectorComposer<Component> {

    @Wire
    Button uploadLog;

    @Wire
    Button existingLog;

    private PageConfigurationProvider pageProvider;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        pageProvider = MasterConfiguration.getInstance().getPageConfigurationProvider();

        wireButtons();
    }

    private void wireButtons() {
        uploadLog.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                setContent("uploadLog");
            }
        });

        existingLog.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                setContent("existingLog");
            }
        });
    }

    private void setContent(String destination) {
        Include include = (Include) Selectors.iterable(getPage(), "#contentInclude").iterator().next();
        include.setSrc(pageProvider.getByPageName(destination).getUri());
    }
}
