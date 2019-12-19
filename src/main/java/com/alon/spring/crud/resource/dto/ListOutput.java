package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public class ListOutput<T> {
    
    protected List<T> content;
    protected int page;
    protected int pageSize;
    protected int totalPages;
    protected int totalSize;

    public List<T> getContent() {
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
    
    public static <L extends ListOutput> ListOutputBuilder<L> of(Class<L> type) {
        return new ListOutputBuilder<>(type);
    }
    
    public static <L extends ListOutput, T extends BaseEntity, O> ListOutput<O> 
        of(Page<T> page, Projection<T, O> outputConverter) {
        
        return new ListOutputBuilder(ListOutput.class)
                .page(page.getNumber() + 1)
                .pageSize(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalSize(Long.valueOf(page.getTotalElements()).intValue())
                .content(page.getContent()
                             .stream()
                             .map(value -> outputConverter.project(value))
                             .collect(Collectors.toList()))
                .build();
        
    }
    
    public static class ListOutputBuilder<L extends ListOutput> {
        
        private L output;
        
        private ListOutputBuilder(Class<L> type) {
            try {
                this.output = type.getConstructor().newInstance();
            } catch (Exception ex) {
                throw new InternalError(ex);
            }
        }
        
        public <C> ListOutputBuilder content(List<C> content) {
            this.output.content = content;
            return this;
        }
        
        public ListOutputBuilder<L> page(int page) {
            this.output.page = page;
            return this;
        }
        
        public ListOutputBuilder<L> pageSize(int pageSize) {
            this.output.pageSize = pageSize;
            return this;
        }
        
        public ListOutputBuilder<L> totalPages(int totalPages) {
            this.output.totalPages = totalPages;
            return this;
        }
        
        public ListOutputBuilder<L> totalSize(int totalSize) {
            this.output.totalSize = totalSize;
            return this;
        }
        
        public L build() {
            return this.output;
        }
        
    }
    
}
