package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EntityListOutput<T extends BaseEntity> extends ListOutput<T> {

    public EntityListOutput() {
    }

    public EntityListOutput(List<T> content, int page, int pageSize, int totalPages, int totalSize) {
        super(content, page, pageSize, totalPages, totalSize);
    }
    
}
