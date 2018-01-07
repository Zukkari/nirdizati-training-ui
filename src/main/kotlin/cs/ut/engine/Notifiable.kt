package cs.ut.engine

import cs.ut.engine.events.NirdizatiEvent

interface Notifiable {
    fun onUpdate(event: NirdizatiEvent)

    fun isAlive(): Boolean
}