package cs.ut.engine;

import cs.ut.config.MasterConfiguration;
import cs.ut.config.nodes.DirectoryPathConfiguration;
import cs.ut.exceptions.NirdizatiRuntimeException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileWriter {
    private static final Logger log = Logger.getLogger(FileWriter.class);

    private String scriptDir;
    private String userModelDir;
    private String coreDir;
    private String datasetDir;
    private String trainingDir;
    private String pklDir;

    public FileWriter() {
        DirectoryPathConfiguration pathProvider = MasterConfiguration.getInstance().getDirectoryPathConfiguration();
        scriptDir = pathProvider.getScriptDirectory();
        userModelDir = pathProvider.getUserModelDirectory();
        coreDir = scriptDir.concat("core/");
        datasetDir = pathProvider.getDatasetDirectory();
        trainingDir = pathProvider.getTrainDirectory();
        pklDir = pathProvider.getPklDirectory();
    }


    public void writeJsonToDisk(JSONObject json, String filename, String path) {
        log.debug("Writing json to disk...");

        File file = new File(coreDir.concat(path.concat(filename.concat(".json"))));

        byte[] bytes;
        try {
            bytes = json.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.debug(String.format("Unsupported encoding %s", e));
            throw new NirdizatiRuntimeException(e);
        }

        if (!file.exists()) {
            try {
                Files.createFile(Paths.get(file.getAbsolutePath()));
                log.debug(String.format("Created file <%s>", file.getName()));
            } catch (IOException e) {
                throw new NirdizatiRuntimeException(String.format("Failed creating file <%s>", file.getAbsolutePath()), e);
            }
        }

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
            os.close();
            log.debug(String.format("Successfully written json to disk... <%s> bytes written", bytes.length));
        } catch (IOException e) {
            throw new NirdizatiRuntimeException(String.format("Failed writing json file to disk <%s>", file.getName()), e);
        }
    }
}
