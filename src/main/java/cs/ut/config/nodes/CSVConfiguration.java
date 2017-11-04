package cs.ut.config.nodes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "csvConfig")
public class CSVConfiguration {

    @XmlElementWrapper(name = "userCols")
    @XmlElement(name = "col")
    private List<String> userCols;

    @XmlElementWrapper(name = "caseId")
    @XmlElement(name = "id")
    private List<String> caseId;

    @XmlElementWrapper(name = "activityId")
    @XmlElement(name = "id")
    private List<String> activityId;

    @XmlElementWrapper(name = "timestampFormat")
    @XmlElement(name = "format")
    private List<String> timestampFormat;

    @XmlElement(name = "splitter")
    private String splitter;

    @XmlElementWrapper(name = "emptyValues")
    @XmlElement(name = "id")
    private List<String> emptyValues;

    @XmlElement(name = "threshold")
    private Integer threshold;

    public List<String> getUserCols() {
        return userCols;
    }

    public List<String> getCaseId() {
        return caseId;
    }

    public List<String> getActivityId() {
        return activityId;
    }

    public List<String> getTimestampFormat() {
        return timestampFormat;
    }

    public String getSplitter() {
        return splitter;
    }

    public List<String> getEmptyValues() {
        return emptyValues;
    }

    public Integer getThreshold() {
        return threshold;
    }
}
