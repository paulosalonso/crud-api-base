package com.alon.spring.crud.domain.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
public class EntityTest extends BaseEntity<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String stringProperty;

    public static Builder of() {
        return new Builder();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public static final class Builder {

        private EntityTest entityTest;

        public Builder() {
            entityTest = new EntityTest();
        }

        public Builder id(Long id) {
            entityTest.setId(id);
            return this;
        }

        public Builder stringProperty(String stringProperty) {
            entityTest.setStringProperty(stringProperty);
            return this;
        }

        public EntityTest build() {
            return entityTest;
        }
    }
}
