package com.alon.spring.crud.domain.model;

import com.alon.spring.crud.domain.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
public class EntityTest extends BaseEntity<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String stringProperty;

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
}
