package cs.ut.controllers.training;

import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.Property;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.AdvancedModeProvider;
import cs.ut.ui.providers.GeneratorArgument;
import org.apache.log4j.Logger;
import org.zkoss.zul.Vlayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancedModeController extends AbstractModeController implements ModeController {
    private static final Logger log = Logger.getLogger(AdvancedModeController.class);

    private NirdizatiGrid<GeneratorArgument> grid;

    private Map<String, List<Property>> hyperparameters = new HashMap<>();

    protected AdvancedModeController(Vlayout vlayout) {
        super(vlayout);
    }

    /* TODO
     * 2. Create listener that will add different hyperparameter grids
     * 3. Implement validation logic
     * 4. Implement value gathering.
     */

    @Override
    public void init() {
        log.debug("Initializing advanced mode controller...");
        gridContainer.getChildren().clear();
        grid = new NirdizatiGrid<>(new AdvancedModeProvider());

        grid.generate(parameters
                .entrySet()
                .stream()
                .map(it -> new GeneratorArgument(it.getKey(), it.getValue())).collect(Collectors.toList()), true);

        gridContainer.appendChild(grid);
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
