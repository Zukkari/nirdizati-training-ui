package cs.ut.manager;

import cs.ut.config.MasterConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LogManager {
    private String logDirectory = MasterConfiguration.getInstance().getUserLogDirectory();

    private static LogManager manager;

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
     * @return List of all available file names contained in user log directory
     */
    public List<File> getAllAvailableLogs() {
        List<File> logs = new ArrayList<>();

        File folder = new File(logDirectory);

        File[] files = folder.listFiles();
        if (files != null) {
            logs.addAll(Arrays.stream(files).filter(it -> ".XES".equalsIgnoreCase(getFileExtension(it.getName()))).collect(Collectors.toList()));
        }

        return logs;
    }

    /**
     * Retrieves file extension of the file name.
     *
     * @param name where file extension should be extracted from.
     * @return file extension or empty string if file extension denoting character could not be found.
     */
    public String getFileExtension(String name) {
        int lastIndex = name.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        } else {
            return name.substring(lastIndex);
        }
    }
}
