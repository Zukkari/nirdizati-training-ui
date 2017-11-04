package cs.ut.engine.item;

import java.util.*;

public class Case {
    private String id;
    private Map<String, Set<String>> attributes = new LinkedHashMap<>();
    private List<String> staticCols = new ArrayList<>();
    private List<String> dynamicCols = new ArrayList<>();

    private Map<String, List<String>> classifiedColumns = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Set<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Set<String>> attributes) {
        this.attributes = attributes;
    }

    public List<String> getStaticCols() {
        return staticCols;
    }

    public void setStaticCols(List<String> staticCols) {
        this.staticCols = staticCols;
    }

    public List<String> getDynamicCols() {
        return dynamicCols;
    }

    public void setDynamicCols(List<String> dynamicCols) {
        this.dynamicCols = dynamicCols;
    }

    public Map<String, List<String>> getClassifiedColumns() {
        return classifiedColumns;
    }

    public void setClassifiedColumns(Map<String, List<String>> classifiedColumns) {
        this.classifiedColumns = classifiedColumns;
    }
}
