package cs.ut.jobs

import cs.ut.engine.JobManager

class JobService {
    companion object {

        fun findSimilarJobs(key: String, job: SimulationJob): List<SimulationJob> {
            val allJobs = JobManager.getCompletedJobs(key)
            return allJobs.filter { it.logFile == job.logFile && it.outcome.parameter == job.outcome.parameter && it.id != job.id }
        }
    }
}