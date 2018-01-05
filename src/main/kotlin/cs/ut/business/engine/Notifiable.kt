package cs.ut.business.engine

import cs.ut.business.jobs.Job

interface Notifiable {
    fun onUpdate(key: String, job: Job)

    fun onDeploy(key: String, jobs: List<Job>) = Unit
}