package cs.ut.config.items;

import cs.ut.config.items.ModelParameter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class ModelProperties {
    private List<String> types;
    private List<ModelParameter> parameters;

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

    public void setParameters(List<ModelParameter> parameters) {
        this.parameters = parameters;
    }
}
