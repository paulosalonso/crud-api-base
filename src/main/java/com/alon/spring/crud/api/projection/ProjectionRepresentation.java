package com.alon.spring.crud.api.projection;

import java.util.Map;

public class ProjectionRepresentation {

    public String projectionName;
    public boolean singleDefault;
    public boolean collectionDefault;
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

    public boolean isSingleDefault() {
        return singleDefault;
    }

    public void setSingleDefault(boolean singleDefault) {
        this.singleDefault = singleDefault;
    }

    public boolean isCollectionDefault() {
        return collectionDefault;
    }

    public void setCollectionDefault(boolean collectionDefault) {
        this.collectionDefault = collectionDefault;
    }

    public Map<String, Object> getRepresentation() {
        return representation;
    }

    public void setRepresentation(Map<String, Object> representation) {
        this.representation = representation;
    }

}
