package cs.ut.controllers.training;

import cs.ut.config.items.ModelParameter;

import java.util.List;
import java.util.Map;

public interface ModeController {
    void init();
    boolean isValid();
    Map<String, List<ModelParameter>> gatherValues();
}
