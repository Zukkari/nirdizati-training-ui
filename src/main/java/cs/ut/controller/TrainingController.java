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

    private Row hyperParamRow = new Row();

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
                option = new ModelParameter(option);

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

                row.appendChild(checkbox);
            });

            gridRows.appendChild(row);
        });
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

        properties.forEach((key, value ) -> {
            if ("predictiontype".equals(key)) return;

            Row row = new Row();
            row.setSclass("option-row");

            Label label = new Label(Labels.getLabel(key));
            label.setSclass("option-label");

            row.appendChild(label);

            Combobox combobox = new Combobox();
            combobox.setId(key);
            value.forEach(val -> {
                val = new ModelParameter(val);

                if (val.isEnabled()) {
                    Comboitem comboitem = combobox.appendItem(Labels.getLabel(key.concat(".").concat(val.getId())));
                    comboitem.setValue(val);
                }
            });

            combobox.addEventListener(Events.ON_CHANGE,
                    (SerializableEventListener<Event>) event -> parameters.put(combobox.getId(), Collections.singletonList(combobox.getSelectedItem().getValue())));
            combobox.setReadonly(true);
            combobox.setSelectedItem(combobox.getItemAtIndex(0));
            parameters.put(combobox.getId(), Collections.singletonList(combobox.getSelectedItem().getValue()));

            row.appendChild(combobox);
            gridRows.appendChild(row);

            if ("learner".equals(key)) {
                gridRows.appendChild(hyperParamRow);
                combobox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) e -> generateHyperparambox(combobox.getSelectedItem().getValue()));
                generateHyperparambox(combobox.getSelectedItem().getValue());
            }
        });
    }

    private void generateHyperparambox(ModelParameter option) {
        hyperParamRow.getChildren().clear();

        hyperParamRow.appendChild(new Label());

        Grid grid = new Grid();
        grid.setVflex("min");
        grid.setHflex("min");

        Rows rows = new Rows();

        grid.appendChild(rows);

        log.debug("Generating additional hyperparameter fields for learners");

        if (option.getEstimators() != null) {
            Row cont = new Row();
            cont.appendChild(new Label(Labels.getLabel(option.getType().concat(".").concat("option_estimators"))));

            Intbox estimators = new Intbox();
            estimators.setValue(option.getEstimators());
            estimators.setConstraint(NO_EMPTY);

            estimators.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> option.setEstimators(estimators.getValue()));
            estimators.addEventListener(Events.ON_ERROR, (SerializableEventListener<Event>) event -> estimators.setValue(0));

            cont.appendChild(estimators);
            rows.appendChild(cont);
        }

        if (option.getMaxfeatures() != null) {
            Row cont = new Row();
            cont.appendChild(new Label(Labels.getLabel(option.getType().concat(".").concat("option_maxfeatures"))));

            Doublebox doublebox = new Doublebox();
            doublebox.setValue(option.getMaxfeatures());
            doublebox.setConstraint(NO_EMPTY);

            doublebox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> option.setMaxfeatures(doublebox.getValue()));
            doublebox.addEventListener(Events.ON_ERROR, (SerializableEventListener<Event>) event -> doublebox.setValue(0.0));

            cont.appendChild(doublebox);
            rows.appendChild(cont);
        }

        if (option.getGbmrate() != null) {
            Row cont = new Row();
            cont.appendChild(new Label(Labels.getLabel(option.getType().concat(".").concat("option_gbmrate"))));

            Doublebox doublebox = new Doublebox();
            doublebox.setValue(option.getGbmrate());
            doublebox.setConstraint(NO_EMPTY);

            doublebox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> option.setGbmrate(doublebox.getValue()));
            doublebox.addEventListener(Events.ON_ERROR, (SerializableEventListener<Event>) event -> doublebox.setValue(0.0));

            cont.appendChild(doublebox);
            rows.appendChild(cont);
        }

        hyperParamRow.appendChild(grid);
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
                Comboitem comboitem = predictionType.getSelectedItem();

                Map<String, List<ModelParameter>> params = new HashMap<>(parameters);
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
