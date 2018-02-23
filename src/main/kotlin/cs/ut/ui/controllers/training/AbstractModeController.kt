package cs.ut.ui.controllers.training

import cs.ut.config.items.ModelParameter
import cs.ut.providers.ModelParamProvider
import cs.ut.ui.controllers.TrainingController
import org.zkoss.zul.Vlayout

abstract class AbstractModeController(protected val gridContrainer: Vlayout) {
    protected val provider = ModelParamProvider()

    protected val parameters: Map<String, List<ModelParameter>> by lazy {
        (provider.properties - TrainingController.PREDICTION)
    }
}