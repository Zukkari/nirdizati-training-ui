package cs.ut.config.nodes;

import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.ModelProperties;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelConfigurationConfiguration {
    private static final Logger log = Logger.getLogger(ModelConfigurationConfiguration.class);

    private List<String> initalTypes;
    private List<ModelParameter> initialParameters;

    private Map<String, List<ModelParameter>> properties = new LinkedHashMap<>();

    private ModelConfigurationConfiguration() {
    }

    public ModelConfigurationConfiguration(ModelProperties parameters) {
        this.initalTypes = parameters.getTypes();
        this.initialParameters = parameters.getParameters();

        validateAndClassify();
    }

    private void validateAndClassify() {
        assert initalTypes != null : "Expected valid inital types";
        assert initialParameters != null : "Expected valid initial parameters";

        initalTypes.forEach(it -> properties.put(it, new ArrayList<>()));

        initialParameters.forEach(it -> {
            if (initalTypes.contains(it.getType())) {
                properties.get(it.getType()).add(it);
            } else {
                throw new IllegalArgumentException(
                        String.format("Invalid value %s. Allowed parameters are <%s>",
                                it.getType(), initalTypes));
            }
        });

        log.debug("Completed classification for model parameters");
    }

    public Map<String, List<ModelParameter>> getProperties() {
        return new LinkedHashMap<>(properties);
    }
}