package com.alon.spring.crud.api.controller.input;

import java.util.List;

public class Options {

    private List<String> expand;
    private String projection;

    public List<String> getExpand() {
        return expand;
    }

    public void setExpand(List<String> expand) {
        this.expand = expand;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

}
