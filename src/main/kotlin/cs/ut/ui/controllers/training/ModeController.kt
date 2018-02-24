package cs.ut.ui.controllers.training

import cs.ut.engine.item.ModelParameter

interface ModeController {
    fun isValid(): Boolean

    fun gatherValues(): Map<String, List<ModelParameter>>

    fun preDestroy() = Unit
}