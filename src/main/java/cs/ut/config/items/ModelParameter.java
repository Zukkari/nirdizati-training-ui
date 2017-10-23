package cs.ut.config.items;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean class that represents model properties
 */

@XmlType(propOrder = {"id", "parameter", "type", "enabled"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelParameter {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "parameter")
    private String parameter;

    @XmlElement(name = "type")
    private String type;

    @XmlElement(name = "enabled")
    private boolean enabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }
}
