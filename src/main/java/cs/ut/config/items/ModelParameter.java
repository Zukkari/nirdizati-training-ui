package cs.ut.config.items;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean class that represents model properties
 */

@XmlType(propOrder = {"id", "parameter", "type", "enabled", "estimators", "maxfeatures", "gbmrate"})
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

    @XmlElement(name = "estimators")
    private Integer estimators;

    @XmlElement(name = "maxfeatures")
    private Double maxfeatures;

    @XmlElement(name = "gbmrate")
    private Double gbmrate;

    public ModelParameter() {
    }

    public ModelParameter(ModelParameter parameter) {
        this.id = parameter.getId();
        this.parameter = parameter.getParameter();
        this.type = parameter.getType();
        this.enabled = parameter.isEnabled();
        this.estimators = parameter.getEstimators();
        this.maxfeatures = parameter.getMaxfeatures();
        this.gbmrate = parameter.getGbmrate();
    }

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

    public Integer getEstimators() {
        return estimators;
    }

    public void setEstimators(Integer estimators) {
        this.estimators = estimators;
    }

    public Double getMaxfeatures() {
        return maxfeatures;
    }

    public void setMaxfeatures(Double maxfeatures) {
        this.maxfeatures = maxfeatures;
    }

    public Double getGbmrate() {
        return gbmrate;
    }

    public void setGbmrate(Double gbmrate) {
        this.gbmrate = gbmrate;
    }
}
