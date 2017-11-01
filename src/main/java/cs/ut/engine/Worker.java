package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.engine.item.Job;
import cs.ut.provider.DirectoryPathProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread {

    private static final Logger log = Logger.getLogger(Worker.class);
    private static Worker worker;

    private String scriptDir;
    private String userModelDir;
    private String coreDir;
    private String datasetDir;
    private String trainingDir;

    private Queue<Job> jobQueue = new LinkedList<>();

    private Worker() {
    }

    public static Worker getInstance() {
        if (worker == null) {
            worker = new Worker();

            DirectoryPathProvider pathProvider = MasterConfiguration.getInstance().getDirectoryPathProvider();
            worker.scriptDir = pathProvider.getScriptDirectory();
            worker.userModelDir = pathProvider.getUserModelDirectory();
            worker.coreDir = worker.scriptDir.concat("core/");
            worker.datasetDir = pathProvider.getDatasetDirectory();
            worker.trainingDir = pathProvider.getTrainDirectory();
        }
        return worker;
    }

    @Override
    public void run() {
        while (true) {
            if (jobQueue.peek() != null) {
                Job job = jobQueue.poll();
                try {
                    generateTrainingJson(job);
                    log.debug(String.format("Executing job <%s>", job));
                    job.setStartTime(Calendar.getInstance().getTime());
                    executeJob(job);
                } catch (Exception e) {
                    log.debug(String.format("Failed to execute job <%s>", job), e);
                }
                job.setCompleteTime(Calendar.getInstance().getTime());
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void scheduleJob(Job job) {
        jobQueue.add(job);
    }


    private void executeJob(Job job) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python",
                    coreDir.concat("train.py"),
                    coreDir.concat("BPIC15_4.csv"),
                    job.getBucketing().getParameter(),
                    job.getEncoding().getParameter(),
                    job.getLearner().getParameter());

            pb.directory(new File(coreDir));
            pb.inheritIO();
            Map<String, String> env = pb.environment();
            env.put("PYTHONPATH", scriptDir);

            Process process = pb.start();
            log.debug(pb.command());
            if (!process.waitFor(180, TimeUnit.SECONDS)) {
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

            String noExtensionName = FilenameUtils.getBaseName(job.getLog().getName());
            File dir = new File(userModelDir.concat(noExtensionName));
            if (!dir.exists() && !dir.mkdir()) {
                throw new RuntimeException(String.format("Cannot create folder for model with name <%s>", dir.getName()));
            }

            Files.move(Paths.get(coreDir.concat(job.toString())), Paths.get(userModelDir.concat(noExtensionName.concat("/")).concat(job.toString())), StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully moved user model to user model storage directory");

            job.setResultPath(Paths.get(userModelDir.concat(job.toString())).toString());
            JobManager.getInstance().getCompletedJobs().add(job);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute script call", e);
        }
    }

    private void generateTrainingJson(Job job) {
        JSONObject json = new JSONObject();

        ModelParameter learner = job.getLearner();

        JSONObject params = new JSONObject();
        params.put("max_features", learner.getMaxfeatures() == null ? 0.0 : learner.getMaxfeatures());
        params.put("n_estimators", learner.getEstimators() == null ? 0 : learner.getEstimators());
        params.put("gbm_learning_rate", learner.getGbmrate() == null ? 0 : learner.getGbmrate());
        params.put("n_clusters", 1);

        json.put(job.getOutcome().getParameter(),
                new JSONObject().put(job.getBucketing().getParameter().concat("_").concat(job.getEncoding().getParameter()),
                        new JSONObject().put(learner.getParameter(), params)));

        log.debug(String.format("Generated following json based on config %n <%s>", json.toString()));

        writeJsonToDisk(json, FilenameUtils.getBaseName(job.getLog().getName()), trainingDir);
    }

    private void writeJsonToDisk(JSONObject json, String filename, String path) {
        log.debug("Writing json to disk...");

        File file = new File(coreDir.concat(path.concat(filename.concat(".json"))));

        byte[] bytes;
        try {
            bytes = json.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.debug(String.format("Unsupported encoding %s", e));
            throw new RuntimeException(e);
        }

        if (!file.exists()) {
            try {
                Files.createFile(Paths.get(file.getAbsolutePath()));
                log.debug(String.format("Created file <%s>", file.getName()));
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed creating file <%s>", file.getAbsolutePath()), e);
            }
        }

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
            os.close();
            log.debug(String.format("Successfully written json to disk... <%s> bytes written", bytes.length));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed writing json file to disk <%s>", file.getName()), e);
        }
    }
}
