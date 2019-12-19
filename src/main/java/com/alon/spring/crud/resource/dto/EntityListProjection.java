package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component("default_list")
public class EntityListProjection<T extends BaseEntity> implements Projection<Page<T>, ListOutput<T>> {
    
    @Autowired
    private EntityProjection<T> entityProjection;
    
    @Override
    public ListOutput project(Page<T> input) {
        return ListOutput.of(input, entityProjection);
    }

}
