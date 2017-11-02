package cs.ut.provider;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement(name = "paths")
public class DirectoryPathProvider {

    @XmlElement(name = "userLogDirectory")
    private String userLogDirectory;

    @XmlElement(name = "userModelDirectory")
    private String userModelDirectory;

    @XmlElement(name = "scriptDirectory")
    private String scriptDirectory;

    public String getUserLogDirectory() {
        return userLogDirectory;
    }

    public String getUserModelDirectory() {
        return userModelDirectory;
    }

    public String getScriptDirectory() {
        return scriptDirectory;
    }

    public void validatePathsExist() {
        File file = new File(userLogDirectory);

        if (!file.exists() && !file.mkdir()) {
            throw new RuntimeException(String.format("Cannot write to directory <%s>", userLogDirectory));
        }

        file = new File(userModelDirectory);

        if (!file.exists() && !file.mkdir()) {
            throw new RuntimeException(String.format("Cannot write to directory <%s>", userLogDirectory));
        }
    }
}
