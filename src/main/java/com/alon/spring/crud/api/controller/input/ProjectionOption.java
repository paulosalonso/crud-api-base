package com.alon.spring.crud.api.controller.input;

import com.alon.spring.crud.core.validation.ValidProjection;

public class ProjectionOption {

    @ValidProjection
    private String projection;

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }
}
