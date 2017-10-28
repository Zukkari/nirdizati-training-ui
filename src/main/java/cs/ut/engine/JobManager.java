package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.engine.item.Job;
import cs.ut.provider.DirectoryPathProvider;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JobManager {
    private static final Logger log = Logger.getLogger(JobManager.class);
    private static JobManager jobManager;
    private String scriptDir;
    private String userModelDir;
    private String coreDir;

    private Queue<Job> jobQueue = new LinkedList<>();
    private List<Job> completedJobs = new ArrayList<>();

    private String logName;

    private JobManager() {
    }

    public static JobManager getInstance() {
        if (jobManager == null) {
            log.debug("Initializing job manager");
            jobManager = new JobManager();

            DirectoryPathProvider pathProvider = MasterConfiguration.getInstance().getDirectoryPathProvider();
            jobManager.scriptDir = pathProvider.getScriptDirectory();
            jobManager.userModelDir = pathProvider.getUserModelDirectory();
            jobManager.coreDir = jobManager.scriptDir.concat("core/");
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

                            jobQueue.add(job);
                        })));
    }

    public synchronized void runJobs() {
        while (jobQueue.peek() != null) {
            Job job = jobQueue.poll();
            job.setStartTime(Calendar.getInstance().getTime());
            executeJob(job);
            job.setCompleteTime(Calendar.getInstance().getTime());
        }
        logName = null;
    }


    private void executeJob(Job job) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python",
                    coreDir.concat("train.py"),
                    coreDir.concat("BPIC15_4.csv"),
                    "bpic15",
                    job.getBucketing().getParameter(),
                    job.getEncoding().getParameter(),
                    job.getLearner().getParameter());

            pb.directory(new File(coreDir));
            pb.inheritIO();
            Map<String, String> env = pb.environment();
            env.put("PYTHONPATH", scriptDir);

            Process process = pb.start();
            log.debug(pb.command());
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroy();
                throw new RuntimeException("Timed out while trying to create predictor");
            }

            log.debug("Script finished running...");

            File file = new File(coreDir.concat(job.toString()));
            log.debug(file);

            if (!file.exists()) {
                throw new RuntimeException("Process failed to finish");
            }

            log.debug("Script exited successfully");
            log.debug(String.format("Trying to move file to user model storage dir <%s>", userModelDir));
            Files.move(Paths.get(coreDir.concat(job.toString())), Paths.get(userModelDir.concat(job.toString())), StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully moved user model to user model storage directory");

            job.setResultPath(Paths.get(userModelDir.concat(job.toString())).toString());
            completedJobs.add(job);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute script call", e);
        }
    }

    public List<Job> getCompletedJobs() {
        return completedJobs;
    }
}
