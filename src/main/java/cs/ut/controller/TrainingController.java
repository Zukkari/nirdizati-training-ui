package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.engine.JobManager;
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
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.*;

import java.io.File;
import java.util.*;

public class TrainingController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(TrainingController.class);
    private static final String NO_EMPTY = "no empty";

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

    private transient Map<String, List<ModelParameter>> parameters = new HashMap<>();

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

    private void initAdvancedMode() {
        optionsGrid.getRows().getChildren().clear();
        log.debug(properties);

        properties.keySet().forEach(key -> parameters.put(key, new ArrayList<>()));

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

                checkbox.addEventListener(Events.ON_CLICK, (SerializableEventListener<Event>) event -> {
                    if (checkbox.isChecked()) {
                        parameters.get(((ModelParameter) checkbox.getValue()).getType()).add(checkbox.getValue());
                    } else {
                        parameters.get(((ModelParameter) checkbox.getValue()).getType()).remove(checkbox.getValue());
                    }
                });

                if ("learner".equals(option.getType())) {
                    Vbox container = new Vbox();
                    container.appendChild(checkbox);
                    Grid optionHolder = new Grid();
                    Rows rows = new Rows();
                    optionHolder.appendChild(rows);
                    container.appendChild(optionHolder);
                    generateHyperparameterFields(rows, checkbox, option);
                    row.appendChild(container);
                } else {
                    row.appendChild(checkbox);
                }
            });

            gridRows.appendChild(row);
        });
    }

    private void generateHyperparameterFields(Rows container, Checkbox checkbox, ModelParameter option) {
        log.debug("Generating additional hyperparameter fields for learners");

        container.setVisible(checkbox.isChecked());
        checkbox.addEventListener(Events.ON_CLICK, e -> container.setVisible(checkbox.isChecked()));
        if (option.getEstimators() != null) {
            Row cont = new Row();
            cont.appendChild(new Label(Labels.getLabel(option.getType().concat(".").concat("option_estimators"))));

            Intbox estimators = new Intbox();
            estimators.setValue(option.getEstimators());
            estimators.setConstraint(NO_EMPTY);

            estimators.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> option.setEstimators(estimators.getValue()));

            cont.appendChild(estimators);
            container.appendChild(cont);
        }

        if (option.getMaxfeatures() != null) {
            Row cont = new Row();
            cont.appendChild(new Label(Labels.getLabel(option.getType().concat(".").concat("option_maxfeatures"))));

            Doublebox doublebox = new Doublebox();
            doublebox.setValue(option.getMaxfeatures());
            doublebox.setConstraint(NO_EMPTY);

            doublebox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> option.setMaxfeatures(doublebox.getValue()));

            cont.appendChild(doublebox);
            container.appendChild(cont);
        }

        if (option.getGbmrate() != null) {
            Row cont = new Row();
            cont.appendChild(new Label(Labels.getLabel(option.getType().concat(".").concat("option_gbmrate"))));

            Doublebox doublebox = new Doublebox();
            doublebox.setValue(option.getGbmrate());
            doublebox.setConstraint(NO_EMPTY);

            doublebox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> option.setGbmrate(doublebox.getValue()));

            cont.appendChild(doublebox);
            container.appendChild(cont);
        }
    }

    private void initPredictions() {
        List<ModelParameter> params = properties.remove("predictiontype");
        log.debug(String.format("Received %s prediction types", params.size()));

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
        initAdvancedMode();
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
            Runnable jobs = () -> {
                JobManager.getInstance().setLog(clientLogs.getSelectedItem().getValue());
                Map<String, List<ModelParameter>> params =
                        basicMode.equals(modeSwitch.getSelectedItem()) ?
                        new HashMap<>(MasterConfiguration.getInstance().getModelConfigurationProvider().getBasicModel())
                        : new HashMap<>(parameters);
                Comboitem comboitem = predictionType.getSelectedItem();
                params.put(((ModelParameter) comboitem.getValue()).getType(), Collections.singletonList(comboitem.getValue()));
                JobManager.getInstance()
                        .generateJobs(params);
            };
            log.debug("Jobs generated...");
            jobs.run();

        }
    }

    private boolean validateData() {
        boolean isOk = true;

        File selectedFile = clientLogs.getSelectedItem().getValue();

        if (selectedFile == null) {
            clientLogs.setErrorMessage(Labels.getLabel("training.file_not_found"));
            isOk = false;
        }

        ModelParameter selectedPrediction = predictionType.getSelectedItem().getValue();
        if (selectedPrediction == null) {
            predictionType.setErrorMessage(Labels.getLabel("training.empty_prediciton"));
            isOk = false;
        }

        return isOk;
    }
}
