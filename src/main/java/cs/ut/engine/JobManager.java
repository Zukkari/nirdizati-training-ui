package cs.ut.engine;

import cs.ut.config.items.ModelParameter;
import cs.ut.engine.item.Job;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class JobManager {
    private static final Logger log = Logger.getLogger(JobManager.class);
    private static JobManager jobManager;

    Queue<Job> jobQueue = new LinkedList<>();
    private String logName;

    private JobManager() {
    }

    public static JobManager getInstance() {
        if (jobManager == null) {
            log.debug("Initializing job manager");
            jobManager = new JobManager();
        }
        return jobManager;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public void generateJobs(Map<String, List<ModelParameter>> parameters) {
        if (logName == null) {
            throw new RuntimeException("Log file is null");
        }
    }
}
