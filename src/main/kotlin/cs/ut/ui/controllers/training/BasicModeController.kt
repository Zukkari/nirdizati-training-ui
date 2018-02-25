package cs.ut.ui.controllers.training

import cs.ut.engine.item.ModelParameter
import cs.ut.logging.NirdizatiLogger
import cs.ut.providers.ModelParamProvider
import cs.ut.ui.UIComponent
import org.zkoss.zk.ui.Component
import org.zkoss.zul.Vlayout

class BasicModeController(gridContainer: Vlayout, private val logName: String) : AbstractModeController(gridContainer),
    ModeController, UIComponent {
    private val log = NirdizatiLogger.getLogger(BasicModeController::class.java, getSessionId())
    private val optimized: Map<String, List<ModelParameter>> = ModelParamProvider.getOptimizedParameters()

    init {
        log.debug("Initializing basic mode controller")
        this.gridContainer.getChildren<Component>().clear()
    }

    override fun isValid(): Boolean = true

    override fun gatherValues(): Map<String, List<ModelParameter>> =
        if (logName in optimized) {
            log.debug("Found optimized parameters for log $logName")
            optimized[logName]!!.groupBy { it.type }
        } else {
            log.debug("Did not find optimized params for log $log. Using default params")
            provider.getBasicParameters().groupBy { it.type }
        }
}