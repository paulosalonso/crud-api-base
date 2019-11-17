package com.alon.spring.crud.resource.dto;

import java.util.List;

public abstract class ListOutput<T> {

    public ListOutput() {}

    public ListOutput(List<T> content, int page, int pageSize, int totalPages, int totalSize) {
        this.content = content;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalSize = totalSize;
    }
    
    public List<T> content;
    public int page;
    public int pageSize;
    public int totalPages;
    public int totalSize;
    
}
