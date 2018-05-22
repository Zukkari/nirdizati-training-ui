package cs.ut.ui.context.operations

import cs.ut.jobs.SimulationJob

class DeleteJobOperation(context: SimulationJob) : Operation<SimulationJob>(context) {

    override fun perform() {

    }

    override fun isEnabled(): Boolean {
        return false
    }
}