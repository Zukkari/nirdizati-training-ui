package cs.ut.controllers;

import cs.ut.config.ClientInfo;
import cs.ut.config.MasterConfiguration;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.East;
import org.zkoss.zul.Include;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller that responds for the content of the index page
 * Purpose of the class is to not write a lot of repeatable code.
 * If this class did not exist then every controller should contain setContent method which would be
 * exactly the same for each class.
 */

public class MainPageController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(MainPageController.class);
    private static MainPageController mainPageController;
    private final transient Map<Session, ClientInfo> clientInformation = new HashMap<>();

    @Wire
    private Borderlayout mainLayout;

    @Wire
    private East trackerEast;


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        mainPageController = this;
    }

    /**
     * Instantiates the controller if it is the first access.
     *
     * @return MainPageController that allows to set content of the page asynchronously.
     */
    public static MainPageController getInstance() {
        return mainPageController;
    }

    @Listen("onClientInfo = #mainLayout")
    public void gatherInformation(ClientInfoEvent event) {
        log.debug("Gathering client browser information...");
        ClientInfo clientInfo = new ClientInfo(
                event.getScreenWidth(),
                event.getScreenHeight(),
                event.getDesktopWidth(),
                event.getDesktopHeight(),
                event.getColorDepth(),
                event.getOrientation(),
                (event.getDesktopHeight() - 100) / 280
        );

        clientInformation.put(Executions.getCurrent().getSession(), clientInfo);
        configureTracker(clientInfo);
        log.debug("Finished gather client information: ");
    }

    /**
     * Returns information about current sessions browser
     *
     * @param session which information to fetch
     * @return nullable if information aobut session does not exist
     */
    public ClientInfo getClientInfo(Session session) {
        return clientInformation.get(session);
    }

    private void configureTracker(ClientInfo clientInfo) {
        if (trackerEast != null) {
            trackerEast.setSize(clientInfo.getWindowWidth() * 0.25 + "px");
            trackerEast.setVisible(false);
        }
    }

    /**
     * Sets content of the page. Since application content is located in a single component, then we change is
     * asynchronously. This is done using this method.
     *
     * @param destination - id of the page to which the content should be changed (defined in configuration.xml)
     * @param page        - caller page where Include element should be looked for.
     */
    public void setContent(String destination, Page page) {
        getPage().setTitle(Labels.getLabel("header.".concat(destination)).concat(" - Nirdizati"));
        Include include = (Include) Selectors.iterable(page, "#contentInclude").iterator().next();
        include.setSrc(null);
        include.setSrc(MasterConfiguration.getInstance().getPageConfiguration().getByPageName(destination).getUri());
    }

    public Component getComp() {
        return getSelf();
    }
}
