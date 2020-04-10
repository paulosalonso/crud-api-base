package com.alon.spring.crud.api.controller.output;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.alon.spring.crud.api.controller.projection.Projector;
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
    
    public static OutputPageBuilder of() {
        return new OutputPageBuilder();
    }
    
    public static <T extends BaseEntity, O> OutputPage of(Page<T> page) {
        
        return new OutputPageBuilder()
                .page(page.getNumber())
                .pageSize(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalSize(Long.valueOf(page.getTotalElements()).intValue())
                .content(page.getContent())
                .build();
        
    }
    
    public static <T extends BaseEntity, O> OutputPage of(Page<T> page, Projector<T, O> projector) {
        
        return new OutputPageBuilder()
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
    
    public static final class OutputPageBuilder {
        
        private OutputPage output;
        
        private OutputPageBuilder() {
            try {
                this.output = new OutputPage();
            } catch (Exception ex) {
                throw new InternalError(ex);
            }
        }
        
        public <C> OutputPageBuilder content(List<C> content) {
            this.output.content = content;
            return this;
        }
        
        public OutputPageBuilder page(int page) {
            this.output.page = page;
            return this;
        }
        
        public OutputPageBuilder pageSize(int pageSize) {
            this.output.pageSize = pageSize;
            return this;
        }
        
        public OutputPageBuilder totalPages(int totalPages) {
            this.output.totalPages = totalPages;
            return this;
        }
        
        public OutputPageBuilder totalSize(int totalSize) {
            this.output.totalSize = totalSize;
            return this;
        }
        
        public OutputPage build() {
            return this.output;
        }
        
    }
    
}
