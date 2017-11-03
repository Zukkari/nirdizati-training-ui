package cs.ut.engine.item;

import java.util.*;

public class Case {
    private String id;
    private Map<String, Set<String>> attributes = new LinkedHashMap<>();
    private Map<String, List<String>> classifiedCols = new HashMap<>();

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

    public Map<String, List<String>> getClassifiedCols() {
        return classifiedCols;
    }

    public void setClassifiedCols(Map<String, List<String>> classifiedCols) {
        this.classifiedCols = classifiedCols;
    }
}
