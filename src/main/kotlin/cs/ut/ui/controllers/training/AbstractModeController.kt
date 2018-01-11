package cs.ut.ui.controllers.training

import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import cs.ut.ui.controllers.TrainingController
import org.zkoss.zul.Vlayout

abstract class AbstractModeController(protected val gridContrainer: Vlayout) {
    protected val parameters: Map<String, List<ModelParameter>> by lazy {
        (MasterConfiguration.modelConfiguration.properties - TrainingController.PREDICTION)
    }
}