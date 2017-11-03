package cs.ut.manager;

import cs.ut.config.MasterConfiguration;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LogManager {
    private String logDirectory = MasterConfiguration.getInstance().getDirectoryPathConfiguration().getUserLogDirectory();

    private static LogManager manager;

    private List<String> allowedExtensions = MasterConfiguration.getInstance().getExtensions();

    private LogManager() {
    }

    public static LogManager getInstance() {
        if (manager == null) {
            manager = new LogManager();
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
}
