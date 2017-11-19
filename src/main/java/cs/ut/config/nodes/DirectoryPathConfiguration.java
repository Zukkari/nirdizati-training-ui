package cs.ut.config.nodes;

import cs.ut.exceptions.NirdizatiRuntimeException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement(name = "paths")
public class DirectoryPathConfiguration {

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

    public void validatePathsExist() {
        File file = new File(userLogDirectory);
        createDirIfAbsent(file);

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

    private void createDirIfAbsent(File file) {
        if (!file.exists() && !file.mkdir()) {
            throw new NirdizatiRuntimeException(String.format("Cannot write to directory <%s>", userLogDirectory));
        }
    }
}
