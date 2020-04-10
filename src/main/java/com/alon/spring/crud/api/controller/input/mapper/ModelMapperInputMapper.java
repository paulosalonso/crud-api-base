package com.alon.spring.crud.api.controller.input.mapper;

import com.alon.spring.crud.domain.model.BaseEntity;
import org.modelmapper.ModelMapper;

public class ModelMapperInputMapper<I, O extends BaseEntity<?>> implements InputMapper<I, O> {

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
        if (outputType.isAssignableFrom(input.getClass()))
            return (O) input;

        return modelMapper.map(input, outputType);
    }

}
