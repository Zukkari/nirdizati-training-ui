package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Include;

public class MainPageController extends SelectorComposer<Component> {
    private static MainPageController mainPageController;

    static MainPageController getInstance() {
        if (mainPageController == null) {
            mainPageController = new MainPageController();
        }

        return mainPageController;
    }

    void setContent(String destination, Page page) {
        Include include = (Include) Selectors.iterable(page, "#contentInclude").iterator().next();
        include.setSrc(MasterConfiguration.getInstance().getPageConfigurationProvider().getByPageName(destination).getUri());
    }
}
