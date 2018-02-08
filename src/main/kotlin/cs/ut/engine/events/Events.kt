package cs.ut.engine.events

import cs.ut.jobs.Job

sealed class NirdizatiEvent

data class StatusUpdateEvent(val data: Job) : NirdizatiEvent()

data class DeployEvent(val target: String, val data: List<Job>) : NirdizatiEvent()