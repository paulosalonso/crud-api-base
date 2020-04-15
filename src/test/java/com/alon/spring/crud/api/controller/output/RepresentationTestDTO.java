package com.alon.spring.crud.api.controller.output;

import java.util.List;

import com.alon.spring.crud.api.controller.output.EntityTestDTO;

public class RepresentationTestDTO {
    private Long id;
    private EntityTestDTO dto;
    private List<EntityTestDTO> objectList;
    private EntityTestDTO[] objectArray;
    private List<String> stringList;
    private String[] stringArray;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityTestDTO getDto() {
        return dto;
    }

    public List<EntityTestDTO> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<EntityTestDTO> objectList) {
        this.objectList = objectList;
    }

    public EntityTestDTO[] getObjectArray() {
        return objectArray;
    }

    public void setObjectArray(EntityTestDTO[] objectArray) {
        this.objectArray = objectArray;
    }

    public void setDto(EntityTestDTO dto) {
        this.dto = dto;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }
}
