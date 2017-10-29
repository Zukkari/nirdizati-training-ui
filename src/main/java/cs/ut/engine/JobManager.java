package cs.ut.engine;

import cs.ut.config.items.ModelParameter;
import cs.ut.engine.item.Job;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobManager {
    private static final Logger log = Logger.getLogger(JobManager.class);

    private static JobManager jobManager;
    private List<Job> completedJobs = new ArrayList<>();

    private File logFile;

    private JobManager() {
    }

    public static JobManager getInstance() {
        if (jobManager == null) {
            jobManager = new JobManager();
        }
        return jobManager;
    }

    public void setLog(File log) {
        this.logFile = log;
    }

    public void generateJobs(Map<String, List<ModelParameter>> parameters) {
        if (logFile == null) {
            throw new RuntimeException("Log file is null");
        }

        List<ModelParameter> encodings = parameters.get("encoding");
        List<ModelParameter> bucketing = parameters.get("bucketing");
        List<ModelParameter> learner = parameters.get("learner");
        List<ModelParameter> result = parameters.get("predictiontype");

        encodings.forEach(encoding ->
                bucketing.forEach(bucket ->
                        learner.forEach(learn -> {
                            Job job = new Job();
                            job.setEncoding(encoding);
                            job.setBucketing(bucket);
                            job.setLearner(learn);
                            job.setOutcome(result.get(0));
                            job.setLog(logFile);

                            log.debug(String.format("Scheduled job <%s>", job));
                            Worker.getInstance().scheduleJob(job);
                        })));

        if (!Worker.getInstance().isAlive()) {
            Worker.getInstance().start();
        }

        logFile = null;
    }

    public List<Job> getCompletedJobs() {
        return completedJobs;
    }
}
