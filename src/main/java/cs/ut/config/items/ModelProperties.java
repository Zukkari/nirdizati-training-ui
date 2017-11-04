package cs.ut.config.items;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "modelConfig")
public class ModelProperties {
    private List<String> types;
    private List<ModelParameter> parameters;
    private List<String> basicParams;

    @XmlElementWrapper(name = "types")
    @XmlElement(name = "type")
    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    @XmlElementWrapper(name = "modelparams")
    @XmlElement(name = "modelparam")
    public List<ModelParameter> getParameters() {
        return parameters;
    }

    @XmlElementWrapper(name = "basicParams")
    @XmlElement(name = "param")
    public List<String> getBasicParams() {
        return basicParams;
    }

    public void setBasicParams(List<String> basicParams) {
        this.basicParams = basicParams;
    }

    public void setParameters(List<ModelParameter> parameters) {
        this.parameters = parameters;
    }
}
