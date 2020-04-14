package com.alon.spring.crud.api.controller.output;

public class EntityTestDTO {
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

        private EntityTestDTO entityTestDTO;

        public Builder() {
            entityTestDTO = new EntityTestDTO();
        }

        public Builder id(Long id) {
            entityTestDTO.setId(id);
            return this;
        }

        public Builder property(String property) {
            entityTestDTO.setProperty(property);
            return this;
        }

        public EntityTestDTO build() {
            return entityTestDTO;
        }
    }
}
