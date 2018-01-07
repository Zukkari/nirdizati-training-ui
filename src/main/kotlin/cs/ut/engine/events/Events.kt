package cs.ut.engine.events

import cs.ut.jobs.Job

sealed class NirdizatiEvent(open val target: String)

data class StatusUpdateEvent(override val target: String, val data: Job) : NirdizatiEvent(target)

data class DeployEvent(override val target: String, val data: List<Job>) : NirdizatiEvent(target)