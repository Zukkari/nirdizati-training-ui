package cs.ut.engine.item;

import cs.ut.config.items.ModelParameter;

import java.util.Calendar;
import java.util.Date;

public class Job {
    private ModelParameter encoding;
    private ModelParameter bucketing;
    private ModelParameter regression;

    private String log;

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

    public ModelParameter getRegression() {
        return regression;
    }

    public void setRegression(ModelParameter regression) {
        this.regression = regression;
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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

}
