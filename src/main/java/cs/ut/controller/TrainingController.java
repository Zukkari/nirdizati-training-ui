package cs.ut.controller;

import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.Property;
import cs.ut.engine.JobManager;
import cs.ut.manager.LogManager;
import cs.ut.ui.FieldComponent;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.GeneratorArgument;
import cs.ut.ui.providers.ModelParamToCombo;
import cs.ut.ui.providers.PropertyValueProvider;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Vlayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrainingController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(TrainingController.class);
    private static final String LEARNER = "learner";
    private static final String ENCODING = "encoding";
    private static final String BUCKETING = "bucketing";
    private static final String PREDICTION = "predictiontype";

    @Wire
    private Combobox clientLogs;

    @Wire
    private Combobox predictionType;

    @Wire
    private Checkbox advancedMode;

    @Wire
    private Button startTraining;

    @Wire
    private Vlayout gridContainer;

    private NirdizatiGrid<Property> parameterGrid;

    private transient Map<String, List<ModelParameter>> parameters = MasterConfiguration.getInstance().getModelConfiguration().getProperties();

    private transient List<Property> hyperParameters = new ArrayList<>();
    private transient List<Component> valueProviders = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.debug("Initialized TrainingController");
        parameterGrid = new NirdizatiGrid<>(new PropertyValueProvider());
        parameterGrid.setVisible(false);
        parameterGrid.setHflex("min");

        initClientLogs();
        initPredictions();
        initBasicMode();
    }

    private void initBasicMode() {
        gridContainer.getChildren().clear();
        NirdizatiGrid<GeneratorArgument> grid = new NirdizatiGrid<>(new ModelParamToCombo());
        grid.setHflex("min");

        grid.generate(
                parameters
                        .entrySet()
                        .stream()
                        .map(it -> new GeneratorArgument(it.getKey(), it.getValue()))
                        .collect(Collectors.toList()), true);

        valueProviders.addAll(grid.getFields().stream().map(FieldComponent::getControl).collect(Collectors.toList()));
        valueProviders.forEach(this::generateListener);

        gridContainer.appendChild(grid);
        gridContainer.appendChild(parameterGrid);
    }

    private void generateListener(Component control) {
        control.addEventListener(Events.ON_SELECT, e -> {
            log.debug(String.format("Combobox %s control changed, regenerating parameter grid.", control));
            log.debug(String.format("Old hyperparemeters are: %s", hyperParameters));
            hyperParameters.clear();
            hyperParameters.addAll(((ModelParameter) ((Comboitem) ((SelectEvent) e).getSelectedItems().iterator().next()).getValue()).getProperties());
            List<Component> components = valueProviders.stream().filter(it -> !it.equals(control)).collect(Collectors.toList());

            for (Component component : components) {
                Combobox combobox = (Combobox) component;
                List<Property> properties = ((ModelParameter) combobox.getSelectedItem().getValue()).getProperties();
                hyperParameters.addAll(properties);
            }
            log.debug(String.format("New hyperparameters: %s", hyperParameters));
            parameterGrid.generate(hyperParameters, true);

            if (!hyperParameters.isEmpty()) {
                parameterGrid.setVisible(true);
            }
        });
    }

    private void initPredictions() {
        List<ModelParameter> params = parameters.remove(PREDICTION);
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

    @Listen("onCheck = #advancedMode")
    public void enabledAdvanced() {
        if (advancedMode.isChecked()) {
            log.debug("enabling advanced mode");
//            initAdvancedMode();
        } else {
            log.debug("enabling basic mode");
            initBasicMode();
        }
    }

    @Listen("onClick = #startTraining")
    public void startTraining() {
        if (validateData()) {
            return;
        }
        log.debug("Parameters are valid, calling script to construct the model");
        Runnable jobs = () -> {
            JobManager.Manager.setLogFile(clientLogs.getSelectedItem().getValue());
            Comboitem comboitem = predictionType.getSelectedItem();

            Map<String, List<ModelParameter>> jobParameters = new HashMap<>();
            jobParameters.put(PREDICTION, Lists.newArrayList((ModelParameter)comboitem.getValue()));

            if (!advancedMode.isChecked()) {
                Component grid = gridContainer.getChildren().get(0);
                if (grid instanceof NirdizatiGrid) {
                    NirdizatiGrid g = (NirdizatiGrid) grid;
                    Map<String, Object> vals = g.gatherValues();

                    for (Map.Entry<String, Object> param : vals.entrySet()) {
                        String key = param.getKey();
                        ModelParameter val = new ModelParameter((ModelParameter) param.getValue());

                        if (LEARNER.equals(val.getType())) {
                                /* Set hyperparams */
                            List<Property> properties = parameterGrid.gatherValues()
                                    .entrySet()
                                    .stream()
                                    .map(it -> new Property(it.getKey(), "", it.getValue().toString()))
                                    .collect(Collectors.toList());
                            val.getProperties().clear();
                            val.setProperties(properties);
                        }
                        jobParameters.put(key, Lists.newArrayList(val));
                    }
                }
                JobManager.Manager.generateJobs(jobParameters);
            }

            JobManager.Manager.deployJobs();
        };

        log.debug("Jobs generated...");
        jobs.run();
    }

    private boolean validateData() {
        if (advancedMode.isChecked()) {
            return true;
        } else {
            boolean valid = true;
            for (Component comp : gridContainer.getChildren()) {
                if (comp instanceof NirdizatiGrid && ((NirdizatiGrid) comp).validate()) {
                    valid = false;
                }
            }
            return valid;
        }
    }
}
