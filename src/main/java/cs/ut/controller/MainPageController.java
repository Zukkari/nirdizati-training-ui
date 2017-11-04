package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Include;

/**
 * Controller that responds for the content of the index page
 * Purpose of the class is to not write a lot of repeatable code.
 * If this class did not exist then every controller should contain setContent method which would be
 * exactly the same for each class.
 */

public class MainPageController extends SelectorComposer<Component> {
    private static MainPageController mainPageController;

    /**
     * Instantiates the controller if it is the first access.
     * @return MainPageController that allows to set content of the page asynchronously.
     */
    static MainPageController getInstance() {
        if (mainPageController == null) {
            mainPageController = new MainPageController();
        }

        return mainPageController;
    }

    /**
     * Sets content of the page. Since application content is located in a single component, then we change is
     * asynchronously. This is done using this method.
     * @param destination - id of the page to which the content should be changed (defined in configuration.xml)
     * @param page - caller page where Include element should be looked for.
     */
    void setContent(String destination, Page page) {
        Include include = (Include) Selectors.iterable(page, "#contentInclude").iterator().next();
        include.setSrc(MasterConfiguration.getInstance().getPageConfiguration().getByPageName(destination).getUri());
    }
}
