package cs.ut.ui.controllers.training

import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import org.apache.log4j.Logger
import org.zkoss.zk.ui.Component
import org.zkoss.zul.Vlayout

class BasicModeController(gridContainer: Vlayout, private val logName: String) : AbstractModeController(gridContainer), ModeController {
    private val log: Logger = Logger.getLogger(BasicModeController::class.java)!!
    private val optimized: Map<String, List<ModelParameter>> = MasterConfiguration.optimizedParams

    init {
        log.debug("Initializing basic mode controller")
        gridContrainer.getChildren<Component>().clear()
    }

    override fun isValid(): Boolean = true

    override fun gatherValues(): Map<String, List<ModelParameter>> {
        if (logName in optimized) {
            log.debug("Found optimized parameters for log $logName")
            return optimized[logName]!!.groupBy { it.type }
        } else {
            log.debug("Did not find optimized params for log $log. Using default params")
            return MasterConfiguration.modelConfiguration.basicParameters.groupBy { it.type }
        }
    }
}