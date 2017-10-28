package cs.ut.engine.item;

import cs.ut.config.items.ModelParameter;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class Job {
    private ModelParameter encoding;
    private ModelParameter bucketing;
    private ModelParameter learner;
    private ModelParameter outcome;

    private File log;

    private Date createTime;
    private Date startTime;
    private Date completeTime;

    private String resultPath;

    public Job() {
        createTime = Calendar.getInstance().getTime();
    }


    public ModelParameter getEncoding() {
        return encoding;
    }

    public void setEncoding(ModelParameter encoding) {
        this.encoding = encoding;
    }

    public ModelParameter getBucketing() {
        return bucketing;
    }

    public void setBucketing(ModelParameter bucketing) {
        this.bucketing = bucketing;
    }

    public ModelParameter getLearner() {
        return learner;
    }

    public void setLearner(ModelParameter learner) {
        this.learner = learner;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public File getLog() {
        return log;
    }

    public void setLog(File log) {
        this.log = log;
    }

    public ModelParameter getOutcome() {
        return outcome;
    }

    public void setOutcome(ModelParameter outcome) {
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        return outcome.getParameter()
                .concat("_")
                .concat(bucketing.getParameter())
                .concat("_")
                .concat(encoding.getParameter())
                .concat("_")
                .concat(learner.getParameter())
                .concat("_")
                .concat("bpic15")
                .concat(".pkl");
    }
}
