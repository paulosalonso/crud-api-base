package com.alon.spring.crud.service;

import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SearchCriteria {

    private Specification filter;
    private List<String> order;
    private EntityGraph expand;
    private int page;
    private int size;

    public Specification getFilter() {
        return filter;
    }

    public List<String> getOrder() {
        return order;
    }

    public EntityGraph getExpand() {
        return expand;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public SearchType getSearchOption() {

        String option = "";

        if (this.filter != null)
            option += SearchType.FILTER.getOption();

        if (this.order != null)
            option += SearchType.ORDER.getOption();

        if (this.expand != null)
            option += SearchType.EXPAND.getOption();

        return SearchType.getByOptionString(option);

    }

    public static SearchCriteriaBuilder of() {
        return new SearchCriteriaBuilder();
    }

    public static final class SearchCriteriaBuilder {

        private SearchCriteria searchCriteria;

        private SearchCriteriaBuilder() {
            this.searchCriteria = new SearchCriteria();
        }

        public SearchCriteriaBuilder filter(Specification filter) {
            this.searchCriteria.filter = filter;
            return this;
        }

        public SearchCriteriaBuilder order(List<String> order) {
            if (order != null)
                this.searchCriteria.order = order;

            return this;
        }

        public SearchCriteriaBuilder expand(List<String> expand) {
            if (!expand.isEmpty())
                this.searchCriteria.expand = new DynamicEntityGraph(expand);

            return this;
        }

        public SearchCriteriaBuilder page(int page) {
            this.searchCriteria.page = page;
            return this;
        }

        public SearchCriteriaBuilder size(int size) {
            this.searchCriteria.size = size;
            return this;
        }

        public SearchCriteria build() {
            return this.searchCriteria;
        }

    }

}
