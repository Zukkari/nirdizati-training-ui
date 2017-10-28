package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.engine.JobManager;
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

import java.io.File;
import java.util.*;

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

    private transient List<ModelParameter> parameters;

    private transient File selectedFile;
    private transient ModelParameter selectedPrediction;

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
                checkbox.setDisabled(!option.isEnabled());

                row.appendChild(checkbox);
            });

            gridRows.appendChild(row);
        });
    }

    private void initPredictions() {
        List<ModelParameter> params = properties.remove("predictiontype");
        log.debug(String.format("Received %s prediction types", params.size()));

        parameters = new ArrayList<>();
        parameters.addAll(params);

        params.forEach(it -> {
            Comboitem item = predictionType.appendItem(Labels.getLabel(it.getType().concat(".").concat(it.getId())));
            item.setValue(it);
        });

        predictionType.setSelectedItem(predictionType.getItemAtIndex(0));
        predictionType.setReadonly(true);
    }

    private void initClientLogs() {
        LogManager manager = LogManager.getInstance();
        List<File> fileNames = manager.getAllAvailableLogs();
        log.debug(String.format("Got %s items for client log combobox", fileNames.size()));

        fileNames.forEach(file -> {
            Comboitem item = clientLogs.appendItem(file.getName());
            item.setValue(file);
        });

        if (!clientLogs.getItems().isEmpty()) {
            clientLogs.setSelectedItem(clientLogs.getItemAtIndex(0));
        } else {
            clientLogs.setDisabled(true);
        }
        clientLogs.setWidth("250px");
        clientLogs.setReadonly(true);
    }

    private void initBasicMode() {
        optionsGrid.getRows().getChildren().clear();
        Map<String, List<ModelParameter>> basicParams = MasterConfiguration.getInstance().getModelConfigurationProvider().getBasicModel();

        basicParams.forEach((key, value) -> {
            if (!value.isEmpty()) {
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
            }
        });
        log.debug(basicParams);
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

    @Listen("onClick = #startTraining")
    public void startTraining() {
        if (validateData()) {
            log.debug("Parameters are valid, calling script to construct the model");
            if (basicMode.equals(modeSwitch.getSelectedItem())) {
                log.debug("Basic mode training initialized");

                Runnable jobs = () -> {
                    JobManager.getInstance().setLogName(((File) clientLogs.getSelectedItem().getValue()).getName());
                    Map<String, List<ModelParameter>> params = new HashMap<>(MasterConfiguration.getInstance().getModelConfigurationProvider().getBasicModel());
                    Comboitem comboitem = predictionType.getSelectedItem();
                    params.put(((ModelParameter)comboitem.getValue()).getType(), Collections.singletonList(comboitem.getValue()));
                    JobManager.getInstance()
                            .generateJobs(params);
                    JobManager.getInstance().runJobs();
                };

                log.debug("Jobs started...");
                jobs.run();
            }
        }
    }

    private boolean validateData() {
        boolean isOk = true;

        selectedFile = clientLogs.getSelectedItem().getValue();

        if (selectedFile == null) {
            clientLogs.setErrorMessage(Labels.getLabel("training.file_not_found"));
            isOk = false;
        }

        selectedPrediction = predictionType.getSelectedItem().getValue();
        if (selectedPrediction == null) {
            predictionType.setErrorMessage(Labels.getLabel("training.empty_prediciton"));
            isOk = false;
        }

        return isOk;
    }
}
