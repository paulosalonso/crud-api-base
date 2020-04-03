package com.alon.spring.crud.api.controller.input;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public abstract class SearchInput {

    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean expressionPresent() {
        return expression != null;
    }

    public boolean expressionNotPresent() {
        return expression == null;
    }

    public abstract @Nullable Specification toSpecification();
    
}
