package cs.ut.manager;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.DirectoryPathConfiguration;
import cs.ut.exceptions.NirdizatiRuntimeException;
import cs.ut.jobs.SimulationJob;
import cs.ut.util.ConstKt;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LogManager {
    private static final Logger log = Logger.getLogger(LogManager.class);

    private static final String VALIDATION = "validation_";
    private static final String FEATURE = "feat_importance_";
    private static final String DETAILED = "detailed_";

    private String logDirectory = MasterConfiguration.getInstance().getDirectoryPathConfiguration().getUserLogDirectory();
    private String validationDir;
    private String featureImportanceDir;
    private String detailedDir;

    private static LogManager manager;

    private List<String> allowedExtensions = MasterConfiguration.getInstance().getExtensions();

    private LogManager() {
    }

    public static LogManager getInstance() {
        if (manager == null) {
            log.debug("First call to log manager. Initializing...");
            manager = new LogManager();

            DirectoryPathConfiguration config = MasterConfiguration.getInstance().getDirectoryPathConfiguration();
            String scriptDir = config.getScriptDirectory();
            manager.validationDir = scriptDir.concat(config.getValidationDir());
            log.debug(String.format("Validation directory is <%s>", manager.validationDir));
            manager.featureImportanceDir = scriptDir.concat(config.getFeatureDir());
            log.debug(String.format("Feature importance directory is <%s>", manager.featureImportanceDir));
            manager.detailedDir = scriptDir.concat(config.getDetailedDir());
            log.debug(String.format("Detailed info directory is <%s>", manager.detailedDir));
        }

        return manager;
    }

    /**
     * Returns all available file names contained in user log directory defined in configuration.xml
     *
     * @return List of all available file names contained in user log directory
     */
    public List<File> getAllAvailableLogs() {
        List<File> logs = new ArrayList<>();

        File folder = new File(logDirectory);

        File[] files = folder.listFiles();
        if (files != null) {
            logs.addAll(Arrays.stream(files).filter(it -> allowedExtensions.contains(FilenameUtils.getExtension(it.getName()))).collect(Collectors.toList()));
        }

        return logs;
    }


    /**
     * Returns file from configured validation directory that is made as result of given job
     * @param job for which job file should be retrieved
     * @return file that contains job results
     */
    public File getValidationFile(SimulationJob job) {
        log.debug(String.format("Getting validation log for job '%s'", job));
        String fileName = VALIDATION.concat(FilenameUtils.getBaseName(job.toString()));
        return getFile(validationDir.concat(fileName));
    }

    /**
     * Returns file from configured detailed directory that is made as result of given job
     * @param job for which job file should be retrieved
     * @return file that contains job results
     */
    public File getDetailedFile(SimulationJob job) {
        log.debug(String.format("Getting detailed log information for job '%s'", job));
        String fileName = DETAILED.concat(FilenameUtils.getBaseName(job.toString()));
        return getFile(detailedDir.concat(fileName));
    }

    /**
     * Returns either a file or List<File> based on the job. If job uses 'Prefix' encoding then list of files is returned.
     * @param job for which job file should be retrieved
     * @return file or list of files that contain job results
     */
    public Object getFeatureImportanceFiles(SimulationJob job) {
        log.debug(String.format("Fetching feature importance log information for '%s'", job));
        if (ConstKt.PREFIX.equalsIgnoreCase(job.getBucketing().getId())) {
            log.debug("Prefix job, returing list of files");
            List<File> files = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                String fileName = FEATURE.concat(FilenameUtils.getBaseName(job.toString()).concat("_").concat(String.valueOf(i)));
                files.add(getFile(fileName));
            }
            log.debug(String.format("Found <%s> files for job '%s'", files, job));
            return files;
        } else {
            return getFile(FEATURE.concat(FilenameUtils.getBaseName(job.toString())));
        }
    }

    @NotNull
    private File getFile(String fileName) {
        File file = new File(fileName.concat(".csv"));
        log.debug(String.format("Looking for file <%s>", file.getName()));
        if (!file.exists()) {
            throw new NirdizatiRuntimeException(String.format("Validation file '%s' could not be found.", fileName));
        }
        log.debug("File successfully found");
        return file;
    }
}
