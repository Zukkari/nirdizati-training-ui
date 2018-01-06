package cs.ut.engine

import cs.ut.jobs.Job

interface Notifiable {
    fun onUpdate(key: String, job: Job)

    fun onDeploy(key: String, jobs: List<Job>) = Unit
}