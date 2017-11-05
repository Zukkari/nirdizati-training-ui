package cs.ut.jobs;

import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.engine.FileWriter;
import cs.ut.engine.JobManager;
import cs.ut.exceptions.NirdizatiRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.zkoss.util.resource.Labels;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SimulationJob extends Job {
    private static final Logger log = Logger.getLogger(SimulationJob.class);

    private ModelParameter encoding;
    private ModelParameter bucketing;
    private ModelParameter learner;
    private ModelParameter outcome;

    private File logFile;

    public SimulationJob() {
        super();
    }

    @Override
    public String toString() {
        return FilenameUtils.getBaseName(logFile.getName())
                .concat("_")
                .concat(bucketing.getParameter())
                .concat("_")
                .concat(encoding.getParameter())
                .concat("_")
                .concat(learner.getParameter())
                .concat("_")
                .concat(outcome.getParameter())
                .concat(".pkl");
    }

    @Override
    public void preProcess() {
        JSONObject json = new JSONObject();

        JSONObject params = new JSONObject();
        params.put("max_features", learner.getMaxfeatures() == null ? 0.0 : learner.getMaxfeatures());
        params.put("n_estimators", learner.getEstimators() == null ? 0 : learner.getEstimators());
        params.put("gbm_learning_rate", learner.getGbmrate() == null ? 0 : learner.getGbmrate());
        params.put("n_clusters", 1);

        json.put(outcome.getParameter(),
                new JSONObject().put(bucketing.getParameter().concat("_").concat(encoding.getParameter()),
                        new JSONObject().put(learner.getParameter(), params)));


        FileWriter writer = new FileWriter();
        writer.writeJsonToDisk(json, FilenameUtils.getBaseName(logFile.getName()),
                MasterConfiguration.getInstance().getDirectoryPathConfiguration().getTrainDirectory());
    }

    @Override
    public void execute() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python",
                    "train.py",
                    logFile.getName(),
                    bucketing.getParameter(),
                    encoding.getParameter(),
                    learner.getParameter(),
                    outcome.getParameter());

            pb.directory(new File(coreDir));
            pb.inheritIO();
            Map<String, String> env = pb.environment();
            env.put("PYTHONPATH", scriptDir);

            Process process = pb.start();
            log.debug(pb.command());
            if (!process.waitFor(180, TimeUnit.SECONDS)) {
                process.destroy();
                log.debug("Timed out while trying to create predictor");
            }

            log.debug("Script finished running...");

            File file = new File(scriptDir.concat(pklDir).concat(this.toString()));
            log.debug(file);

            if (!file.exists()) {
                log.debug("Process failed to finish");
            }

            log.debug("Script exited successfully");
        } catch (IOException | InterruptedException e) {
            log.debug("Failed to execute script call", e);
        }
    }

    @Override
    public void postExecute() {
        log.debug(String.format("Trying to move file to user model storage dir <%s>", userModelDir));

        String noExtensionName = FilenameUtils.getBaseName(log.getName());
        File dir = new File(userModelDir.concat(noExtensionName));
        if (!dir.exists() && !dir.mkdir()) {
            log.debug(String.format("Cannot create folder for model with name <%s>", dir.getName()));
        }

        try {
            Files.move(Paths.get(scriptDir.concat(pklDir).concat(this.toString())), Paths.get(userModelDir.concat(noExtensionName.concat("/")).concat(this.toString())), StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully moved user model to user model storage directory");

            JobManager.getInstance().getCompletedJobs().add(this);
        } catch (IOException e) {
            log.debug(e);
        }
    }


    public void setEncoding(ModelParameter encoding) {
        this.encoding = encoding;
    }

    public void setBucketing(ModelParameter bucketing) {
        this.bucketing = bucketing;
    }


    public void setLearner(ModelParameter learner) {
        this.learner = learner;
    }

    public ModelParameter getOutcome() {
        return outcome;
    }

    public void setOutcome(ModelParameter outcome) {
        this.outcome = outcome;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public boolean isNotificationRequired() {
        return true;
    }

    @Override
    public String getNotificationMessage() {
        return Labels.getLabel("job.completed.simulation", new Object[]{logFile.getName()});
    }
}
