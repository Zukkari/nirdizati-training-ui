package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.engine.item.Job;
import cs.ut.provider.DirectoryPathProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
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

    private Queue<Job> jobQueue = new LinkedList<>();

    private Worker(){
    }

    public static Worker getInstance() {
        if (worker == null) {
            worker = new Worker();

            DirectoryPathProvider pathProvider = MasterConfiguration.getInstance().getDirectoryPathProvider();
            worker.scriptDir = pathProvider.getScriptDirectory();
            worker.userModelDir = pathProvider.getUserModelDirectory();
            worker.coreDir = worker.scriptDir.concat("core/");
        }
        return worker;
    }

    @Override
    public void run() {
        while (true) {
            if (jobQueue.peek() != null) {
                Job job = jobQueue.poll();
                log.debug(String.format("Executing job <%s>", job));
                job.setStartTime(Calendar.getInstance().getTime());
                executeJob(job);
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
}
