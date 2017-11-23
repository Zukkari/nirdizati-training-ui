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
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

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

    /* TODO
     * 2. Create listener that will add different hyperparameter grids
     * 3. Implement validation logic
     * 4. Implement value gathering for hyperparemeters. (Should be done after step 2)
     */

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
        component.getControl().addEventListener(Events.ON_CHECK, (CheckEvent e) -> {
            log.debug(String.format("%s value changed, regenerating grids", component));
            if (component.getControl() instanceof Checkbox) {
                ModelParameter parameter = ((Checkbox) component.getControl()).getValue();
                if (TrainingController.LEARNER.equals(parameter.getType())) {
                    handleLearner(e, parameter);
                } else {
                    handleOther(parameter);
                }
            }
            generateGrids();
        });
    }

    private void handleOther(ModelParameter parameter) {
        hyperparameters.values().forEach(it -> {
            if (it.containsAll(parameter.getProperties())) {
                it.removeAll(parameter.getProperties());
            } else {
                it.addAll(parameter.getProperties());
            }
        });
    }

    private void handleLearner(CheckEvent e, ModelParameter parameter) {
        if (e.isChecked()) {
            hyperparameters.put(parameter, parameter.getProperties());
        } else {
            hyperparameters.remove(parameter);
        }
    }

    private void generateGrids() {
        hyperParamsContainer.getChildren().clear();
        hyperparameters.entrySet().forEach(this::generateGrid);
    }

    private void generateGrid(Map.Entry<ModelParameter, List<Property>> entry) {
        ModelParameter key = entry.getKey();
        List<Property> values = entry.getValue();
        log.debug("Key: " + key);
        log.debug("Properties: " + values);


        NirdizatiGrid<Property> propertyGrid = new NirdizatiGrid<>(new PropertyValueProvider());
        propertyGrid.setColumns(ImmutableMap.of(Labels.getLabel(key.getType() + "." + key.getId()), "min", "", "min"));
        propertyGrid.generate(values, true);
        propertyGrid.setHflex("min");

        hyperParamsContainer.appendChild(propertyGrid);
    }


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Map<String, List<ModelParameter>> gatherValues() {
        Map<String, Object> gathered = grid.gatherValues();
        Map<String, List<ModelParameter>> retVal = new HashMap<>();

        for (Map.Entry<String, Object> entry : gathered.entrySet()) {
            String key = entry.getKey();
            List<ModelParameter> value = (List<ModelParameter>) entry.getValue();
            retVal.put(key, value);
        }
        return retVal;
    }
}
