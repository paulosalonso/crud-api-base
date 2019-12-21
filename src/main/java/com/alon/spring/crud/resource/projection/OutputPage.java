package com.alon.spring.crud.resource.projection;

import com.alon.spring.crud.model.BaseEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

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
    
    public static <T extends BaseEntity, O> OutputPage
        of(Page<T> page, Projection<T, O> projection) {
        
        return new OutputPageBuilder()
                .page(page.getNumber() + 1)
                .pageSize(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalSize(Long.valueOf(page.getTotalElements()).intValue())
                .content(page.getContent()
                             .stream()
                             .map(projection::project)
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
