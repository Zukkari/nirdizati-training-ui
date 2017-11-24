package cs.ut.controllers.training;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import org.zkoss.zul.Vlayout;

import java.util.List;
import java.util.Map;

public abstract class AbstractModeController {
    protected Vlayout gridContainer;
    protected Map<String, List<ModelParameter>> parameters = MasterConfiguration.getInstance().getModelConfiguration().getProperties();

    protected AbstractModeController(Vlayout vlayout) {
        this.gridContainer = vlayout;
        parameters.remove(TrainingController.PREDICTION);
    }
}
