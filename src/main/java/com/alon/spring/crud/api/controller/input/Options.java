package com.alon.spring.crud.api.controller.input;

import com.alon.spring.crud.core.validation.ValidProjection;

import java.util.Set;

public class Options {

    private Set<String> expand;

    @ValidProjection
    private String projection;

    public Set<String> getExpand() {
        return expand;
    }

    public void setExpand(Set<String> expand) {
        this.expand = expand;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

}
