package com.alon.spring.crud.api.projection;

import java.util.Map;

public class ProjectionRepresentation {

    public String projectionName;
    public Map<String, Object> representation;

    public ProjectionRepresentation() {}

    public ProjectionRepresentation(String projectionName, Map<String, Object> representation) {
        this.projectionName = projectionName;
        this.representation = representation;
    }

    public String getProjectionName() {
        return projectionName;
    }

    public void setProjectionName(String projectionName) {
        this.projectionName = projectionName;
    }

    public Map<String, Object> getRepresentation() {
        return representation;
    }

    public void setRepresentation(Map<String, Object> representation) {
        this.representation = representation;
    }

}
