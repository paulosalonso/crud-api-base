package com.alon.spring.crud.api.controller.output;

import java.util.List;

public class RepresentationTestDTO {
    private Long id;
    private ExampleDTO dto;
    private List<ExampleDTO> objectList;
    private ExampleDTO[] objectArray;
    private List<String> stringList;
    private String[] stringArray;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExampleDTO getDto() {
        return dto;
    }

    public List<ExampleDTO> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<ExampleDTO> objectList) {
        this.objectList = objectList;
    }

    public ExampleDTO[] getObjectArray() {
        return objectArray;
    }

    public void setObjectArray(ExampleDTO[] objectArray) {
        this.objectArray = objectArray;
    }

    public void setDto(ExampleDTO dto) {
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
