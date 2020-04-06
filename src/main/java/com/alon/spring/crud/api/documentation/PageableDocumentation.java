package com.alon.spring.crud.api.documentation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel("Pageable")
public class PageableDocumentation {

    @ApiModelProperty(value = "Page number (default is 0)")
    private int page;

    @ApiModelProperty(value = "Number of elements per page (default is 20)")
    private int size;

    @ApiModelProperty(value = "Property to order,order type (e.g. name, asc)")
    private List<String> sort;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getSort() {
        return sort;
    }

    public void setSort(List<String> sort) {
        this.sort = sort;
    }

}