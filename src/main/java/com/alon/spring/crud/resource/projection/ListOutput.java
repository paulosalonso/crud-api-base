package com.alon.spring.crud.resource.projection;

import com.alon.spring.crud.model.BaseEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class ListOutput {
    
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
    
    public static ListOutputBuilder of() {
        return new ListOutputBuilder();
    }
    
    public static <T extends BaseEntity, O> ListOutput
        of(Page<T> page, Projection<T, O> projection) {
        
        return new ListOutputBuilder()
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
    
    public static final class ListOutputBuilder {
        
        private ListOutput output;
        
        private ListOutputBuilder() {
            try {
                this.output = new ListOutput();
            } catch (Exception ex) {
                throw new InternalError(ex);
            }
        }
        
        public <C> ListOutputBuilder content(List<C> content) {
            this.output.content = content;
            return this;
        }
        
        public ListOutputBuilder page(int page) {
            this.output.page = page;
            return this;
        }
        
        public ListOutputBuilder pageSize(int pageSize) {
            this.output.pageSize = pageSize;
            return this;
        }
        
        public ListOutputBuilder totalPages(int totalPages) {
            this.output.totalPages = totalPages;
            return this;
        }
        
        public ListOutputBuilder totalSize(int totalSize) {
            this.output.totalSize = totalSize;
            return this;
        }
        
        public ListOutput build() {
            return this.output;
        }
        
    }
    
}
