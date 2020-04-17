package com.alon.spring.crud.api.controller.output;

public class ExampleDTO {
    private Long id;
    private String property;

    public static Builder of() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public static final class Builder {

        private ExampleDTO exampleDTO;

        public Builder() {
            exampleDTO = new ExampleDTO();
        }

        public Builder id(Long id) {
            exampleDTO.setId(id);
            return this;
        }

        public Builder property(String property) {
            exampleDTO.setProperty(property);
            return this;
        }

        public ExampleDTO build() {
            return exampleDTO;
        }
    }
}
