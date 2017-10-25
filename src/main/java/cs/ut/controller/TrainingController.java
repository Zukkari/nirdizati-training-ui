package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.manager.LogManager;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.*;

import java.util.List;
import java.util.Map;

public class TrainingController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(TrainingController.class);

    @Wire
    private Combobox clientLogs;

    @Wire
    private Combobox predictionType;

    @Wire
    private Grid optionsGrid;

    @Wire
    private Navbar modeSwitch;

    @Wire
    private Navitem basicMode;

    @Wire
    private Navitem advancedMode;


    private Rows gridRows;

    private transient Map<String, List<ModelParameter>> properties =
            MasterConfiguration.getInstance().getModelConfigurationProvider().getProperties();


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.debug("Initialized TrainingController");

        gridRows = new Rows();
        optionsGrid.appendChild(gridRows);

        initBasicMode();

        initClientLogs();
        initPredictions();

        modeSwitch.setSelectedItem(basicMode);
    }

    private void initOptionsMenu() {
        optionsGrid.getRows().getChildren().clear();
        log.debug(properties);
        properties.forEach((key, value) -> {
            Row row = new Row();
            row.setSclass("option-row");

            Label sectionName = new Label();
            sectionName.setSclass("option-label");
            sectionName.setValue(Labels.getLabel(key));
            row.appendChild(sectionName);

            value.forEach(option -> {
                Checkbox checkbox = new Checkbox();
                checkbox.setName(Labels.getLabel(option.getType().concat(".").concat(option.getId())));
                checkbox.setValue(option);
                checkbox.setLabel(Labels.getLabel(option.getType().concat(".").concat(option.getId())));
                checkbox.setSclass("option-value");

                row.appendChild(checkbox);
            });

            gridRows.appendChild(row);
        });
    }

    private void initPredictions() {
        List<ModelParameter> params = properties.remove("predictiontype");
        log.debug(String.format("Received %s prediction types", params.size()));

        params.forEach(it -> predictionType.appendItem(
                Labels.getLabel(it.getType().concat(".").concat(it.getId()))
        ));
    }

    private void initClientLogs() {
        LogManager manager = LogManager.getInstance();
        List<String> fileNames = manager.getAllAvailableLogs();
        log.debug(String.format("Got %s items for client log combobox", fileNames.size()));

        fileNames.forEach(clientLogs::appendItem);
    }

    private void initBasicMode() {
        optionsGrid.getRows().getChildren().clear();
        Map<String, List<ModelParameter>> parameters = MasterConfiguration.getInstance().getModelConfigurationProvider().getBasicModel();

        parameters.forEach((key, value) -> {
            Row row = new Row();
            row.setSclass("option-row");

            Label caption = new Label(Labels.getLabel(key));
            caption.setSclass("option-label");
            row.appendChild(caption);

            value.forEach(val -> {
                Label label = new Label(Labels.getLabel(key.concat(".").concat(val.getId())));
                label.setSclass("option-value");
                row.appendChild(label);
            });
            gridRows.appendChild(row);
        });

        log.debug(parameters);
    }

    @Listen("onClick = #advancedMode")
    public void enabledAdvanced() {
        log.debug("enabling advanced mode");
        initOptionsMenu();
    }

    @Listen("onClick = #basicMode")
    public void enableBasicMode() {
        log.debug("enabling basic mode");
        initBasicMode();
    }
}
