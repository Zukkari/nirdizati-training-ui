package cs.ut.jobs

import cs.ut.engine.JobManager

class JobService {
    companion object {

        /**
         * Find similar jobs to the given one so they can be used in comparison
         *
         * @param job similar jobs to the given one
         */
        fun findSimilarJobs(job: SimulationJob): List<SimulationJob> {
            val allJobs = JobManager.getJobByPredicate(job.owner)
            return allJobs.filter { it.logFile == job.logFile && it.outcome.id == job.outcome.id && it.id != job.id }
        }
    }
}