package com.alon.spring.crud.api.controller.input;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

public class ExampleSearchInput extends SearchInput {

    private String stringProperty;

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    @Override
    public Specification toSpecification() {
        return (root, query, builder) -> {
            Predicate predicate = null;

            if (stringProperty != null)
                predicate = builder.equal(root.get("stringProperty"), stringProperty);

            return predicate;
        };
    }

}
