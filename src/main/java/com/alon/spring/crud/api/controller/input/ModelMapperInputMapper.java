package com.alon.spring.crud.api.controller.input;

import org.modelmapper.ModelMapper;

public class ModelMapperInputMapper<I, O> implements InputMapper<I, O> {

    private final ModelMapper modelMapper;
    private final Class<O> outputType;

    public ModelMapperInputMapper(Class<O> outputType) {
        this.outputType = outputType;
        this.modelMapper = new ModelMapper();
    }

    public ModelMapperInputMapper(Class<O> outputType, ModelMapper modelMapper) {
        this.outputType = outputType;
        this.modelMapper = modelMapper;
    }

    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    @Override
    public O map(I input) {
        return modelMapper.map(input, outputType);
    }

}
