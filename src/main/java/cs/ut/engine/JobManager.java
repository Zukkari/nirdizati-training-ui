package cs.ut.engine;

import cs.ut.config.items.ModelParameter;
import cs.ut.engine.item.Job;
import cs.ut.exceptions.NirdizatiRuntimeException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;

import java.io.File;
import java.util.*;

public class JobManager {
    private static final Logger log = Logger.getLogger(JobManager.class);

    private static JobManager jobManager;
    private List<Job> completedJobs = new ArrayList<>();

    private Map<Session, Queue<Job>> jobQueue = new HashMap<>();

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
            throw new NirdizatiRuntimeException("Log file is null");
        }

        List<ModelParameter> encodings = parameters.get("encoding");
        List<ModelParameter> bucketing = parameters.get("bucketing");
        List<ModelParameter> learner = parameters.get("learner");
        List<ModelParameter> result = parameters.get("predictiontype");


        Session currentSession = Executions.getCurrent().getSession();

        Queue<Job> list = jobQueue.get(currentSession) == null ? new LinkedList<>() : jobQueue.get(currentSession);
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
                            list.add(job);
                        })));

        jobQueue.put(currentSession, list);
        logFile = null;
    }

    public List<Job> getCompletedJobs() {
        return completedJobs;
    }

    public void delployJobs() {
        Worker worker = Worker.getInstance();

        Queue<Job> currentJobs = jobQueue.get(Executions.getCurrent().getSession());
        log.debug(String.format("Deploying <%s> jobs", currentJobs.size()));

        while (currentJobs.peek() != null) {
            worker.scheduleJob(currentJobs.poll());
        }
        log.debug("Successfully deployed all jobs to worker");
    }

    public void flushJobs() {
        Session session = Executions.getCurrent().getSession();
        log.debug(String.format("Clearing job queue for session <%s>", session));
        jobQueue.get(session).clear();
        log.debug(String.format("Cleared job queue for session <%s>", session));
    }

    public File getCurrentFile() {
        Session session = Executions.getCurrent().getSession();
        log.debug(String.format("Getting current file for session <%s>", session));

        Queue<Job> jobs = jobQueue.get(session);

        if (!jobs.isEmpty()) {
            Job job = jobs.peek();
            if (job != null) {
                return job.getLog();
            }
        }
        throw new NirdizatiRuntimeException("Current execution has no jobs scheduled");
    }

    public ModelParameter getPredictionType() {
        Session session = Executions.getCurrent().getSession();

        Queue<Job> jobs = jobQueue.get(session);

        if (!jobs.isEmpty()) {
            return jobs.peek().getOutcome();
        }
        throw new NirdizatiRuntimeException("Current execution has no jobs scheduled");
    }

    public void applyJSON(JSONObject json) {
        Queue<Job> job = jobQueue.get(Executions.getCurrent().getSession());
        job.forEach(j -> j.setDatasetJson(json));
    }
}
