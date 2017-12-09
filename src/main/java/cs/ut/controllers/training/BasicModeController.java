package cs.ut.controllers.training;

import com.google.common.collect.Lists;
import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.Property;
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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Vlayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BasicModeController extends AbstractModeController implements ModeController {
    private static final Logger log = Logger.getLogger(BasicModeController.class);

    private String logName;
    private Map<String, List<ModelParameter>> optimized = MasterConfiguration.getInstance().getOptimizedParams();

    private NirdizatiGrid<GeneratorArgument> grid;
    private NirdizatiGrid<Property> parameterGrid;
    private List<Property> hyperParameters = new ArrayList<>();
    private List<Component> valueProviders = new ArrayList<>();

    public BasicModeController(Vlayout vlayout, String logName) {
        super(vlayout);
        parameterGrid = new NirdizatiGrid<>(new PropertyValueProvider());
        parameterGrid.setVisible(false);
        parameterGrid.setHflex("min");
        this.logName = logName;
    }

    @Override
    public void init() {
        log.debug("Initializing basic controller");
        gridContainer.getChildren().clear();
        grid = new NirdizatiGrid<>(new ModelParamToCombo());
        log.debug("Generating grid");
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
        log.debug(String.format("Successfully generated grid with parameters <%s>", hyperParameters));

        if (optimized.containsKey(logName)) {
            log.debug("Optimized parameters for log are found, setting them up");
            setUpAdvancedParameters();
        } else {
            log.debug("No optimized params for this log, initializing normal basic mode");
            setUpBasicMode();
        }
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

    private void setUpAdvancedParameters() {
        List<ModelParameter> params = optimized.get(logName);

        log.debug(String.format("Optimal parameters: %s", params));
        String msg = Labels.getLabel("training.found_optimized_params", new Object[]{HtmlEscapers.htmlEscaper().escape(logName)});
        log.debug(String.format("Showing notification message to user '%s'", msg));
        Clients.showNotification(msg, "info", grid, "end_center", 5000, true);

        setUpGridWithParams(params);
        log.debug("Finished initializing optimized parameters.");
    }

    private void setUpBasicMode() {
        log.debug("Setting up basic mode with preset parameters");
        List<ModelParameter> basicParams = MasterConfiguration.getInstance().getModelConfiguration().getBasicParameters();
        log.debug(String.format("Basic parameters: %s", basicParams));

        setUpGridWithParams(basicParams);
        log.debug("Finished setting up basic mode");
    }

    private void setUpGridWithParams(List<ModelParameter> parameters) {
        hyperParameters.clear();
        List<FieldComponent> fields = grid.getFields();
        log.debug(String.format("Got %s fields from grid", fields));
        for (FieldComponent component : fields) {
            Component control = component.getControl();
            if (control instanceof Combobox) {
                parameters.forEach(it -> {
                    Optional<Comboitem> item = ((Combobox) control).getItems().stream().filter(i -> i.getValue().equals(it)).findFirst();
                    item.ifPresent(((Combobox) control)::setSelectedItem);
                    if (item.isPresent()) {
                        List<Property> props = ((ModelParameter) item.get().getValue()).getProperties();
                        hyperParameters.addAll(props);
                    }
                });
            }
        }

        log.debug(String.format("Hyperparams: %s", hyperParameters));
        if (!hyperParameters.isEmpty()) {
            log.debug("Hyperparameters not empty, regenerating grid");
            parameterGrid.generate(hyperParameters, true);
            parameterGrid.setVisible(true);
        }
    }

    @Override
    public boolean isValid() {
        boolean valid = true;
        for (Component comp : gridContainer.getChildren()) {
            if (comp instanceof NirdizatiGrid && !((NirdizatiGrid) comp).validate()) {
                valid = false;
            }
        }
        return valid;
    }

    @Override
    public Map<String, List<ModelParameter>> gatherValues() {
        Map<String, Object> gridValues = grid.gatherValues();
        Map<String, List<ModelParameter>> retVal = new HashMap<>();

        for (Map.Entry<String, Object> entry : gridValues.entrySet()) {
            String key = entry.getKey();
            ModelParameter value = new ModelParameter((ModelParameter) entry.getValue());

            if (TrainingController.LEARNER.equals(value.getType())) {
                setProperties(value);
            }

            retVal.put(key, Lists.newArrayList(value));
        }

        return retVal;
    }

    private void setProperties(ModelParameter value) {
        List<Property> properties = parameterGrid.gatherValues()
                .entrySet()
                .stream()
                .map(it -> new Property(it.getKey(), "", it.getValue().toString(), -1, -1))
                .collect(Collectors.toList());
        value.getProperties().clear();
        value.setProperties(properties);
    }
}
