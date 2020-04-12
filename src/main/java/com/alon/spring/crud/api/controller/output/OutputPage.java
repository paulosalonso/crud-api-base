package com.alon.spring.crud.api.controller.output;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.model.BaseEntity;

public class OutputPage {
    
    protected List content;
    protected int page;
    protected int pageSize;
    protected int totalPages;
    protected int totalSize;

    public List getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalSize() {
        return totalSize;
    }
    
    public static Builder of() {
        return new Builder();
    }
    
    public static <T extends BaseEntity, O> OutputPage of(Page<T> page) {
        
        return new Builder()
                .page(page.getNumber())
                .pageSize(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalSize(Long.valueOf(page.getTotalElements()).intValue())
                .content(page.getContent())
                .build();
        
    }
    
    public static <T extends BaseEntity, O> OutputPage of(Page<T> page, Projector<T, O> projector) {
        
        return new Builder()
                .page(page.getNumber())
                .pageSize(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalSize(Long.valueOf(page.getTotalElements()).intValue())
                .content(page.getContent()
                             .stream()
                             .map(projector::project)
                             .collect(Collectors.toList()))
                .build();
        
    }
    
    public static final class Builder {
        
        private OutputPage output;
        
        private Builder() {
            this.output = new OutputPage();
        }
        
        public <C> Builder content(List<C> content) {
            this.output.content = content;
            return this;
        }
        
        public Builder page(int page) {
            this.output.page = page;
            return this;
        }
        
        public Builder pageSize(int pageSize) {
            this.output.pageSize = pageSize;
            return this;
        }
        
        public Builder totalPages(int totalPages) {
            this.output.totalPages = totalPages;
            return this;
        }
        
        public Builder totalSize(int totalSize) {
            this.output.totalSize = totalSize;
            return this;
        }
        
        public OutputPage build() {
            return this.output;
        }
        
    }
    
}
