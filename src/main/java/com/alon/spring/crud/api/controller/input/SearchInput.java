package com.alon.spring.crud.api.controller.input;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public interface SearchInput {
    
    public @Nullable Specification toSpecification();
    
}
