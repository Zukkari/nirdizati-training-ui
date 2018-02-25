package cs.ut.engine.item

/**
 * Entity that stores information about serialized job
 * @param id of the job
 * @param path to log file of the job
 * @param startTime when job was started (used for sorting jobs in chronological order)
 */
data class UiData(val id: String, val path: String, val startTime: String)