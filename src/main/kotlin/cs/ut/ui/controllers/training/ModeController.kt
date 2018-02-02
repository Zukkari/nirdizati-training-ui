package cs.ut.ui.controllers.training

import cs.ut.config.items.ModelParameter

interface ModeController {
    fun isValid(): Boolean

    fun gatherValues(): Map<String, List<ModelParameter>>

    fun preDestroy() = Unit
}