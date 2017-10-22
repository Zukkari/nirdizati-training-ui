package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.manager.LogManager;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Nav;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.*;

import java.util.List;
import java.util.Map;

public class TrainingController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(TrainingController.class);

    @Wire
    Combobox clientLogs;

    @Wire
    Combobox predictionType;

    @Wire
    Vbox optionsMenu;

    @Wire
    Navbar modeSwitch;

    @Wire
    Navitem basicMode;

    @Wire
    Navitem advancedMode;

    private transient Map<String, List<ModelParameter>> properties =
            MasterConfiguration.getInstance().getModelConfigurationProvider().getProperties();


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.debug("Initialized TrainingController");

        initClientLogs();
        initPredictions();

        modeSwitch.setSelectedItem(basicMode);
    }

    private void initOptionsMenu() {
        optionsMenu.getChildren().clear();
        log.debug(properties);
        properties.forEach((key, value) -> {
            Hbox hbox = new Hbox();
            hbox.setSclass("option-row");

            Label sectionName = new Label();
            sectionName.setSclass("option-label");
            sectionName.setValue(Labels.getLabel(key));
            hbox.appendChild(sectionName);

            Hbox valuesBox = new Hbox();
            valuesBox.setSclass("option-values");
            value.forEach(option -> {
                Checkbox checkbox = new Checkbox();
                checkbox.setName(Labels.getLabel(option.getLabel()));
                checkbox.setValue(option);
                checkbox.setLabel(Labels.getLabel(option.getLabel()));
                checkbox.setSclass("option-value");

                valuesBox.appendChild(checkbox);
            });

            hbox.appendChild(valuesBox);
            optionsMenu.appendChild(hbox);
        });
    }

    private void initPredictions() {
        List<ModelParameter> params = properties.remove("predictiontype");
        log.debug(String.format("Received %s prediction types", params.size()));

        params.forEach(it -> predictionType.appendItem(
                Labels.getLabel(it.getLabel())
        ));
    }

    private void initClientLogs() {
        LogManager manager = LogManager.getInstance();
        List<String> fileNames = manager.getAllAvailableLogs();
        log.debug(String.format("Got %s items for client log combobox", fileNames.size()));

        fileNames.forEach(clientLogs::appendItem);
    }

    @Listen("onClick = #advancedMode")
    public void enabledAdvanced() {
        log.debug("enabling advanced mode");
        initOptionsMenu();
    }

    @Listen("onClick = #basicMode")
    public void enableBasicMode() {
        log.debug("enabling basic mode");
        optionsMenu.getChildren().clear();
    }
}
