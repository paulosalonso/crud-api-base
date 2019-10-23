package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class EntityListOutputConverter<T extends BaseEntity> implements OutputDtoConverter<Page<T>, EntityListOutput> {
    
    @Override
    public EntityListOutput convert(Page<T> data) {
        EntityListOutput output = new EntityListOutput();
        output.content = data.getContent();
        output.page = data.getNumber() + 1;
        output.pageSize = data.getContent().size();
        output.totalPages = data.getTotalPages();
        output.totalSize = data.getNumberOfElements();
        
        return output;
    }
}
