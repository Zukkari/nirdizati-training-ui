package cs.ut.controllers.training;

import cs.ut.config.items.ModelParameter;
import org.zkoss.zul.Vlayout;

import java.util.List;
import java.util.Map;

public class AdvancedModeController extends AbstractModeController implements ModeController {
    protected AdvancedModeController(Vlayout vlayout) {
        super(vlayout);
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Map<String, List<ModelParameter>> gatherValues() {
        return null;
    }
}
