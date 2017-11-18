package cs.ut.engine;

import cs.ut.controller.MainPageController;
import cs.ut.jobs.Job;
import cs.ut.jobs.JobStatus;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class Worker extends Thread {

    private static final Logger log = Logger.getLogger(Worker.class);
    private static Worker worker;

    private Queue<Job> jobQueue = new LinkedList<>();

    private Worker() {
    }

    public static Worker getInstance() {
        if (worker == null || !worker.isAlive()) {
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
                    job.setStatus(JobStatus.PREPARING);
                    JobManager.Manager.notifyOfJobStatusChange(job);
                    job.preProcess();
                } catch (Exception e) {
                    log.debug(String.format("<%s> failed in preprocess stage with exception, aborting job", job), e);
                    job.setStatus(JobStatus.FAILED);
                    JobManager.Manager.notifyOfJobStatusChange(job);
                    continue;
                }

                try {
                    job.setStatus(JobStatus.RUNNING);
                    JobManager.Manager.notifyOfJobStatusChange(job);

                    log.debug(String.format("<%s> started job execution", job));
                    job.execute();
                } catch (Exception e) {
                    log.debug(String.format("<%s> failed in execute stage with exception, aborting job", job), e);
                    job.setStatus(JobStatus.FAILED);
                    JobManager.Manager.notifyOfJobStatusChange(job);
                    continue;
                }

                try {
                    job.setStatus(JobStatus.FINISHING);
                    JobManager.Manager.notifyOfJobStatusChange(job);

                    log.debug(String.format("<%s> started job post execute", job));
                    job.postExecute();
                } catch (Exception e) {
                    log.debug(String.format("<%s> failed in post execute stage with exception", job), e);
                    job.setStatus(JobStatus.FAILED);
                    JobManager.Manager.notifyOfJobStatusChange(job);
                }

                job.setCompleteTime(Calendar.getInstance().getTime());
                job.setStatus(JobStatus.COMPLETED);
                JobManager.Manager.notifyOfJobStatusChange(job);

                if (job.isNotificationRequired()) {
                    Executions.schedule(job.getClient(),
                            e -> Clients.showNotification(
                                    job.getNotificationMessage(), "info"
                                    , MainPageController.getInstance().getComp()
                                    , "bottom_center"
                                    , -1),
                            new Event("jobStatus", null, "complete"));
                }
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
