package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class EntityListOutputConverter<T extends BaseEntity> implements OutputDtoConverter<Page<T>, ListOutput<T>> {
    
    @Autowired
    private EntityOutputConverter<T> entityOutputConverter;
    
    @Override
    public ListOutput convert(Page<T> data) {
        
        return ListOutput.of(data, entityOutputConverter);
    }
}
