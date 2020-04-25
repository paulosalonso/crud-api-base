package com.alon.spring.crud.api.controller.input;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public abstract class SearchInput {

    private String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean filterPresent() {
        return filter != null;
    }

    public boolean filterNotPresent() {
        return filter == null;
    }

    public abstract @Nullable Specification toSpecification();
    
}
