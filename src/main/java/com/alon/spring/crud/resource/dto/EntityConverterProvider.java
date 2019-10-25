package com.alon.spring.crud.resource.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityConverterProvider implements ResourceDtoConverterProvider {

    @Autowired
    private EntityListOutputConverter listConverter;
    
    @Autowired
    private EntityInputConverter inputConverter;
    
    @Autowired
    private EntityOutputConverter outputConverter;
    
    @Override
    public EntityListOutputConverter getListOutputDtoConverter() {
        return this.listConverter;
    }

    @Override
    public EntityOutputConverter getReadOutputDtoConverter() {
        return this.outputConverter;
    }

    @Override
    public EntityInputConverter getCreateInputDtoConverter() {
        return this.inputConverter;
    }

    @Override
    public EntityOutputConverter getCreateOutputDtoConverter() {
        return this.outputConverter;
    }

    @Override
    public EntityInputConverter getUpdateInputDtoConverter() {
        return this.inputConverter;
    }

    @Override
    public EntityOutputConverter getUpdateOutputDtoConverter() {
        return this.outputConverter;
    }
    
}
