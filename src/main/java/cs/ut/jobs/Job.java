package cs.ut.jobs;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.DirectoryPathConfiguration;
import org.zkoss.zk.ui.Desktop;

import java.util.Calendar;
import java.util.Date;

public abstract class Job {
    protected Date createTime;
    protected Date startTime;
    protected Date completeTime;

    protected String scriptDir;
    protected String userModelDir;
    protected String coreDir;
    protected String datasetDir;
    protected String trainingDir;
    protected String pklDir;

    private Desktop client;

    public Job() {
        this.createTime = Calendar.getInstance().getTime();

        DirectoryPathConfiguration pathProvider = MasterConfiguration.getInstance().getDirectoryPathConfiguration();
        scriptDir = pathProvider.getScriptDirectory();
        userModelDir = pathProvider.getUserModelDirectory();
        coreDir = scriptDir.concat("core/");
        datasetDir = pathProvider.getDatasetDirectory();
        trainingDir = pathProvider.getTrainDirectory();
        pklDir = pathProvider.getPklDirectory();
    }

    public abstract void preProcess();

    public abstract void execute();

    public abstract void postExecute();

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public boolean isNotificationRequired() {
        return false;
    }

    public Desktop getClient() {
        return client;
    }

    public void setClient(Desktop client) {
        this.client = client;
    }

    public String getNotificationMessage() {
        return "";
    }
}
