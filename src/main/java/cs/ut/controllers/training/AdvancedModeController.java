package cs.ut.controllers.training;

import com.google.common.collect.ImmutableMap;
import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.Property;
import cs.ut.ui.FieldComponent;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.AdvancedModeProvider;
import cs.ut.ui.providers.GeneratorArgument;
import cs.ut.ui.providers.PropertyValueProvider;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancedModeController extends AbstractModeController implements ModeController {
    private static final Logger log = Logger.getLogger(AdvancedModeController.class);

    private NirdizatiGrid<GeneratorArgument> grid;

    private Map<ModelParameter, List<Property>> hyperparameters = new LinkedHashMap<>();
    private Hlayout hyperParamsContainer;

    protected AdvancedModeController(Vlayout vlayout) {
        super(vlayout);
    }

    @Override
    public void init() {
        log.debug("Initializing advanced mode controller...");
        gridContainer.getChildren().clear();
        grid = new NirdizatiGrid<>(new AdvancedModeProvider());
        hyperParamsContainer = new Hlayout();

        grid.generate(parameters
                .entrySet()
                .stream()
                .map(it -> new GeneratorArgument(it.getKey(), it.getValue())).collect(Collectors.toList()), true);

        gridContainer.appendChild(grid);
        gridContainer.appendChild(hyperParamsContainer);
        grid.getFields().forEach(this::generateListener);
        log.debug("Finished grid initialization");
    }

    private void generateListener(FieldComponent component) {
        ModelParameter parameter = ((Checkbox)component.getControl()).getValue();
        if (TrainingController.LEARNER.equals(parameter.getType())) {
            hyperparameters.put(parameter, new ArrayList<>());
        }

        component.getControl().addEventListener(Events.ON_CHECK, (CheckEvent e) -> {
            log.debug(String.format("%s value changed, regenerating grids", component));
            if (component.getControl() instanceof Checkbox) {
                if (TrainingController.LEARNER.equals(parameter.getType())) {
                    handleLearner(e, parameter);
                } else {
                    handleOther(parameter, e);
                }
            }
            generateGrids();
        });
    }

    private void handleOther(ModelParameter parameter, CheckEvent e) {
        hyperparameters.values().forEach(it -> {
            if (!e.isChecked()) {
                it.removeAll(parameter.getProperties());
            } else {
                it.addAll(parameter.getProperties());
            }
        });
    }

    private void handleLearner(CheckEvent e, ModelParameter parameter) {
        if (e.isChecked()) {
            hyperparameters.get(parameter).addAll(parameter.getProperties());
        } else {
            hyperparameters.get(parameter).removeAll(parameter.getProperties());
        }
    }

    private void generateGrids() {
        hyperParamsContainer.getChildren().clear();
        hyperparameters.entrySet().forEach(this::generateGrid);
    }

    private void generateGrid(Map.Entry<ModelParameter, List<Property>> entry) {
        ModelParameter key = entry.getKey();
        List<Property> values = entry.getValue();

        if (values.size() < 2) return;

        log.debug("Key: " + key);
        log.debug("Properties: " + values);


        NirdizatiGrid<Property> propertyGrid = new NirdizatiGrid<>(new PropertyValueProvider());
        propertyGrid.setColumns(ImmutableMap.of(key.getType() + "." + key.getId(), "min", "", "min"));
        propertyGrid.generate(values, true);
        propertyGrid.setHflex("min");

        hyperParamsContainer.appendChild(propertyGrid);
    }


    @Override
    public boolean isValid() {
        boolean isValid = grid.validate();

        for (Component component : hyperParamsContainer.getChildren()) {
            if (component instanceof NirdizatiGrid && !((NirdizatiGrid) component).validate()) {
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public Map<String, List<ModelParameter>> gatherValues() {
        Map<String, Object> gathered = grid.gatherValues();
        Map<String, List<ModelParameter>> retVal = new HashMap<>();

        Map<String, Map<String, String>> hyperparemeters = new HashMap<>();
        hyperParamsContainer.getChildren().forEach(it -> {
            if (it instanceof NirdizatiGrid) {
                hyperparemeters.put(((NirdizatiGrid) it).getColumns().getFirstChild().getId()
                        , ((NirdizatiGrid) it).gatherValues());
            }
        });

        for (Map.Entry<String, Object> entry : gathered.entrySet()) {
            String key = entry.getKey();
            List<ModelParameter> value = (List<ModelParameter>) entry.getValue();

            String[] keys = key.split("\\.");
            if (keys.length > 1) {
                Map<String, String> hyper = hyperparemeters.get(keys[1]);
                if (hyper != null && key.equals(keys[0])) {
                    value.forEach(it -> {
                        if (it.getId().equals(keys[1])) {
                            it.getProperties().clear();
                            it.getProperties()
                                    .addAll(hyper
                                            .entrySet()
                                            .stream()
                                            .map(h -> new Property(h.getKey(), "", h.getValue()))
                                            .collect(Collectors.toList()));
                        }
                    });
                }
            }

            retVal.put(key, value);
        }
        return retVal;
    }
}
