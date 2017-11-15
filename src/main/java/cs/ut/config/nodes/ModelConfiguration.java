package cs.ut.config.nodes;

import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.ModelProperties;
import cs.ut.config.items.Property;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ModelConfiguration {
    private static final Logger log = Logger.getLogger(ModelConfiguration.class);

    private List<String> initalTypes;
    private List<ModelParameter> initialParameters;

    private Map<String, List<ModelParameter>> properties = new LinkedHashMap<>();

    private List<String> basicParameters = new ArrayList<>();

    private ModelConfiguration() {
    }

    public ModelConfiguration(ModelProperties parameters) {
        this.initalTypes = parameters.getTypes();
        this.initialParameters = parameters.getParameters();
        this.basicParameters = parameters.getBasicParams();

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

    public List<ModelParameter> getInitialParameters() {
        return this.initialParameters;
    }

    public List<ModelParameter> getBasicParameters() {
        List<ModelParameter> params = new ArrayList<>();

        basicParameters.forEach(param -> {
            Optional<ModelParameter> result = properties
                    .values().stream().flatMap(Collection::stream).filter(it -> param.equals(it.getParameter())).findFirst();
            result.ifPresent(params::add);
        });

        return params;
    }

    public List<Property> getAllProperties() {
        return initialParameters
                .stream()
                .map(ModelParameter::getProperties)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
