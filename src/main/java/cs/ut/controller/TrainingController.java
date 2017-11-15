package cs.ut.controller;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.Property;
import cs.ut.engine.JobManager;
import cs.ut.manager.LogManager;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.PropertyValueProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainingController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(TrainingController.class);
    private static final String LEARNER = "learner";

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

    @Wire
    private Button startTraining;


    private Rows gridRows;

    private Row hyperParamRow = new Row();

    private transient Map<String, List<ModelParameter>> parameters = new HashMap<>();

    private transient Map<String, List<ModelParameter>> properties =
            MasterConfiguration.getInstance().getModelConfiguration().getProperties();
    private transient List<ModelParameter> basicParametes = MasterConfiguration.getInstance().getModelConfiguration().getBasicParameters();

    private List<NirdizatiGrid> hyperParameters = new ArrayList<>();
    private List<NirdizatiGrid> combinationGrids = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.debug("Initialized TrainingController");

        gridRows = new Rows();
        optionsGrid.appendChild(gridRows);

        initClientLogs();
        initPredictions();

        initBasicMode();

        modeSwitch.setSelectedItem(basicMode);
    }

    private void initAdvancedMode() {
        optionsGrid.getRows().getChildren().clear();
        log.debug(properties);

        properties.keySet().forEach(key -> parameters.put(key, new ArrayList<>()));

        properties.forEach((key, value) -> {
            Row row = new Row();

            Label sectionName = new Label();
            sectionName.setValue(Labels.getLabel(key));
            row.appendChild(sectionName);

            value.forEach(option -> {
                option = new ModelParameter(option);

                Vbox container = new Vbox();

                Checkbox checkbox = new Checkbox();
                checkbox.setName(Labels.getLabel(option.getType().concat(".").concat(option.getId())));
                checkbox.setValue(option);
                checkbox.setLabel(Labels.getLabel(option.getType().concat(".").concat(option.getId())));
                checkbox.setDisabled(!option.getEnabled());
                container.appendChild(checkbox);

                ModelParameter param = option;

                final PropertyValueProvider propertyValueProvider = new PropertyValueProvider();
                final NirdizatiGrid<Property> grid = new NirdizatiGrid<>(propertyValueProvider);

                checkbox.addEventListener(Events.ON_CLICK, (SerializableEventListener<Event>) event -> {
                    if (checkbox.isChecked()) {
                        parameters.get(((ModelParameter) checkbox.getValue()).getType()).add(checkbox.getValue());
                        hyperParameters.add(grid);
                        grid.setVisible(true);

                        List<Property> props = param.getProperties();
                        if (!LEARNER.equalsIgnoreCase(param.getType()) && !props.isEmpty()) {
                            props.forEach(prop -> {
                                grid.getRows().appendChild(propertyValueProvider.provide(prop));
                                grid.setVflex("min");
                                grid.setHflex("min");
                            });
                            container.appendChild(grid);
                            combinationGrids.add(grid);
                        }
                    } else {
                        parameters.get(((ModelParameter) checkbox.getValue()).getType()).remove(checkbox.getValue());
                        hyperParameters.remove(grid);
                        grid.setVisible(false);
                        container.removeChild(grid);
                        grid.getRows().getChildren().clear();

                        if (combinationGrids.contains(grid)) {
                            combinationGrids.remove(grid);
                        }
                    }
                });

                if (LEARNER.equalsIgnoreCase(option.getType())) {
                    grid.setVisible(false);
                    grid.generate(option.getProperties());
                    Vbox vbox = new Vbox();
                    vbox.appendChild(container);
                    vbox.appendChild(grid);
                    row.appendChild(vbox);
                } else {
                    row.appendChild(container);
                }
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

        Escaper escaper = HtmlEscapers.htmlEscaper();

        fileNames.forEach(file -> {
            Comboitem item = clientLogs.appendItem(escaper.escape(file.getName()));
            item.setValue(file);
        });

        if (!clientLogs.getItems().isEmpty()) {
            clientLogs.setSelectedItem(clientLogs.getItemAtIndex(0));
        } else {
            clientLogs.setDisabled(true);
        }
        clientLogs.setWidth("250px");
        clientLogs.setReadonly(true);

        clientLogs.addEventListener(Events.ON_CHANGE, e -> initBasicMode());
    }

    private void initBasicMode() {
        optionsGrid.getRows().getChildren().clear();

        String logName = FilenameUtils.getBaseName(((File) clientLogs.getSelectedItem().getValue()).getName());

        Map<String, List<ModelParameter>> optimizedParameters = MasterConfiguration.getInstance().getOptimizedParams();
        List<ModelParameter> optimized = null;
        if (optimizedParameters.keySet().contains(logName)) {
            log.debug(String.format("Found optimized parameters for log <%s>", logName));
            optimized = optimizedParameters.get(logName);
            Clients.showNotification(Labels.getLabel("training.found_optimized_params", new Object[] {logName}));
        }

        final List<ModelParameter> lambdaOptimized = optimized;
        properties.forEach((key, value) -> {
            if ("predictiontype".equals(key)) return;

            Row row = new Row();

            Label label = new Label(Labels.getLabel(key));
            label.setId(key);

            row.appendChild(label);
            Combobox combobox = new Combobox();

            combobox.setId(key);
            value.forEach(val -> {
                ModelParameter optimal = lambdaOptimized == null ? null : findMatch(val, lambdaOptimized);
                val = optimal == null ? new ModelParameter(val) : new ModelParameter(optimal);

                if (val.getEnabled()) {
                    Comboitem comboitem = combobox.appendItem(Labels.getLabel(key.concat(".").concat(val.getId())));
                    comboitem.setValue(val);

                    if (optimal != null ||
                            (combobox.getSelectedItem() == null && basicParametes.contains(val)))
                        combobox.setSelectedItem(comboitem);

                    List<Property> props = val.getProperties();
                    if (!props.isEmpty()) {
                        List<Row> propertyRow = new ArrayList<>();
                        combobox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) event -> {
                            if (combobox.getSelectedItem().equals(comboitem)) {
                                props.forEach(property ->
                                        hyperParameters.forEach(grid -> {
                                            Row additional = (Row) grid.getProvider().provide(property);
                                            propertyRow.add(additional);
                                            grid.getRows().appendChild(additional);
                                            grid.getRows().setVflex("min");
                                        })
                                );
                            } else {
                                propertyRow.forEach(prop -> hyperParameters.forEach(grid -> {
                                    grid.getRows().removeChild(prop);
                                    grid.getRows().setVflex("min");
                                }));
                            }
                        });
                    }
                }
            });

            combobox.addEventListener(Events.ON_CHANGE,
                    (SerializableEventListener<Event>) event -> parameters.put(combobox.getId(), Collections.singletonList(combobox.getSelectedItem().getValue())));
            combobox.setReadonly(true);
            parameters.put(combobox.getId(), Collections.singletonList(combobox.getSelectedItem().getValue()));

            row.appendChild(combobox);
            gridRows.appendChild(row);

            if (LEARNER.equals(key)) {
                gridRows.appendChild(hyperParamRow);
                combobox.addEventListener(Events.ON_CHANGE, (SerializableEventListener<Event>) e -> generateHyperparambox(combobox.getSelectedItem().getValue()));
                generateHyperparambox(combobox.getSelectedItem().getValue());
            }
        });
    }

    private ModelParameter findMatch(ModelParameter parameter, List<ModelParameter> optimized) {
        Optional<ModelParameter> optional = optimized.stream().filter(it -> it.getId().equalsIgnoreCase(parameter.getId())).findFirst();
        return optional.orElse(null);
    }

    private void generateHyperparambox(ModelParameter option) {
        hyperParamRow.getChildren().clear();
        hyperParamRow.appendChild(new Label());

        NirdizatiGrid<Property> grid = new NirdizatiGrid<>(new PropertyValueProvider());
        grid.setVflex("min");
        grid.setHflex("min");
        grid.generate(option.getProperties());
        grid.setId(option.getId());

        hyperParameters.clear();
        hyperParameters.add(grid);

        log.debug("Generating additional hyperparameter fields for learners");

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
                JobManager.Manager.setLogFile(clientLogs.getSelectedItem().getValue());
                Comboitem comboitem = predictionType.getSelectedItem();

                Map<String, List<ModelParameter>> params = new HashMap<>();
                parameters.forEach((k, v) -> {
                    List<ModelParameter> modelParameters = new ArrayList<>();
                    v.forEach(param -> modelParameters.add(new ModelParameter(param)));
                    params.put(k, v);
                });

                hyperParameters.forEach(grid ->
                        params.get(LEARNER).forEach(learner -> {
                            if (learner.getId().equals(grid.getId())) {
                                Map<String, Number> gridParams = grid.gatherValues();
                                learner.getProperties().clear();
                                gridParams.forEach((k, v) -> {
                                    Property prop = new Property();
                                    prop.setId(k);
                                    prop.setProperty(String.valueOf(v));
                                    learner.getProperties().add(prop);
                                });
                            }
                        }));

                List<Map<String, Object>> listOfPropertyMaps = combinationGrids
                        .stream()
                        .map(NirdizatiGrid<Property>::gatherValues).collect(Collectors.toList());

                List<Property> additionalProps = new ArrayList<>();
                for (Map<String, Object> map : listOfPropertyMaps) {
                    for (Map.Entry entry : map.entrySet()) {
                        Property property = new Property();
                        property.setId((String) entry.getKey());
                        property.setProperty(String.valueOf(entry.getValue()));
                        additionalProps.add(property);
                    }
                }

                params.get(LEARNER).forEach(learner -> learner.getProperties().addAll(additionalProps));
                params.put(((ModelParameter) comboitem.getValue()).getType(), Collections.singletonList(comboitem.getValue()));
                JobManager.Manager.generateJobs(params);
                JobManager.Manager.deployJobs();
            };

            log.debug("Jobs generated...");
            jobs.run();
        }
    }

    private boolean validateData() {
        final boolean[] isOk = {true};

        File selectedFile = clientLogs.getSelectedItem().getValue();

        if (selectedFile == null) {
            clientLogs.setErrorMessage(Labels.getLabel("training.file_not_found"));
            isOk[0] = false;
        }

        ModelParameter selectedPrediction = predictionType.getSelectedItem().getValue();
        if (selectedPrediction == null) {
            predictionType.setErrorMessage(Labels.getLabel("training.empty_prediciton"));
            isOk[0] = false;
        }

        List<String> errorParams = new ArrayList<>();
        parameters.forEach((key, val) -> {
            if (val.isEmpty()) {
                isOk[0] = false;
                errorParams.add(Labels.getLabel(key));
            }
        });

        if (!errorParams.isEmpty()) {
            Clients.showNotification(
                    Labels.getLabel("training.validation_failed",
                            new Object[]{String.join(", ", errorParams)}),
                    "error", getSelf(), "bottom_center", -1);
        }

        for (NirdizatiGrid grid : hyperParameters) {
            if (!grid.validate()) isOk[0] = false;
        }

        return isOk[0];
    }
}
