package cs.ut.config.nodes;

import cs.ut.exceptions.NirdizatiRuntimeException;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;

@XmlRootElement(name = "paths")
public class DirectoryPathConfiguration {

    @XmlElement(name = "python")
    private String python;

    @XmlElement(name = "userLogDirectory")
    private String userLogDirectory;

    @XmlElement(name = "userModelDirectory")
    private String userModelDirectory;

    @XmlElement(name = "scriptDirectory")
    private String scriptDirectory;

    @XmlElement(name = "datasetDirectory")
    private String datasetDirectory;

    @XmlElement(name = "trainDirectory")
    private String trainDirectory;

    @XmlElement(name = "pklDirectory")
    private String pklDirectory;

    @XmlElement(name = "ohpdir")
    private String ohpdir;

    @XmlElement(name = "detailedDir")
    private String detailedDir;

    @XmlElement(name = "featureDir")
    private String featureDir;

    @XmlElement(name = "validationDir")
    private String validationDir;

    @XmlElement(name = "tmpDir")
    private String tmpDir;

    public String getUserLogDirectory() {
        return userLogDirectory;
    }

    public String getUserModelDirectory() {
        return userModelDirectory;
    }

    public String getScriptDirectory() {
        return scriptDirectory;
    }

    public String getDatasetDirectory() {
        return datasetDirectory;
    }

    public String getTrainDirectory() {
        return trainDirectory;
    }

    public String getPklDirectory() {
        return pklDirectory;
    }

    public String getOhpdir() {
        return ohpdir;
    }

    public String getPython() {
        return python;
    }

    public String getDetailedDir() {
        return detailedDir;
    }

    public String getFeatureDir() {
        return featureDir;
    }

    public String getValidationDir() {
        return validationDir;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public void validatePathsExist() {
        File file = new File(userLogDirectory);
        createDirIfAbsent(file);

        file = new File(tmpDir);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new NirdizatiRuntimeException("No permission to delete tmp dir");
            }
        }
        if (!file.mkdir()) {
            throw new NirdizatiRuntimeException("No permission to create tmp dir");
        }

        file = new File(userModelDirectory);
        createDirIfAbsent(file);

        file = new File(datasetDirectory);
        createDirIfAbsent(file);

        file = new File(trainDirectory);
        createDirIfAbsent(file);

        file = new File(scriptDirectory.concat("core/").concat(datasetDirectory));
        createDirIfAbsent(file);

        file = new File(scriptDirectory.concat("core/").concat(trainDirectory));
        createDirIfAbsent(file);

        file = new File(scriptDirectory.concat(pklDirectory));
        createDirIfAbsent(file);

        file = new File(scriptDirectory.concat("core/").concat(ohpdir));
        createDirIfAbsent(file);
    }

    protected void createDirIfAbsent(File file) {
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
