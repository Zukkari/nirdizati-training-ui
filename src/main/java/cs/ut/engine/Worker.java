package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.config.nodes.DirectoryPathConfiguration;
import cs.ut.jobs.Job;
import cs.ut.jobs.SimulationJob;
import cs.ut.exceptions.NirdizatiRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;

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

    private Queue<Job> jobQueue = new LinkedList<>();

    private Worker() {
    }

    public static Worker getInstance() {
        if (worker == null) {
            worker = new Worker();
        }
        return worker;
    }

    @Override
    public void run() {
        while (true) {
            if (jobQueue.peek() != null) {
                Job job = jobQueue.poll();
                job.setStartTime(Calendar.getInstance().getTime());

                try {
                    log.debug(String.format("<%s> started job preprocess", job));
                    job.preProcess();
                } catch (Exception e) {
                    log.debug(String.format("<%s> failed in preprocess stage with exception, aborting job", job), e);
                    break;
                }

                try {
                    log.debug(String.format("<%s> started job execution", job));
                    job.execute();
                } catch (Exception e) {
                    log.debug(String.format("<%s> failed in execute stage with exception, aborting job", job), e);
                    break;
                }

                try {
                    log.debug(String.format("<%s> started job post execute", job));
                    job.postExecute();
                } catch (Exception e) {
                    log.debug(String.format("<%s> failed in post execute stage with exception", job), e);
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
}
