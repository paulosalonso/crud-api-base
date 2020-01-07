package com.alon.spring.crud.service;

import com.alon.spring.specification.ExpressionSpecification;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SearchCriteria {

    private Specification filter;
    private List<String> order;
    private EntityGraph expand;
    private int page = 1;
    private int pageSize = 100;

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

    public int getPageSize() {
        return pageSize;
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
        
        public SearchCriteriaBuilder filter(String filter) {
            this.searchCriteria.filter = ExpressionSpecification.of(filter);
            return this;
        }

        public SearchCriteriaBuilder order(List<String> order) {
            this.searchCriteria.order = order;
            return this;
        }

        public SearchCriteriaBuilder expand(List<String> expand) {
            if (expand != null && !expand.isEmpty())
                this.searchCriteria.expand = new DynamicEntityGraph(expand);

            return this;
        }

        public SearchCriteriaBuilder page(int page) {
            this.searchCriteria.page = page;
            return this;
        }

        public SearchCriteriaBuilder pageSize(int pageSize) {
            this.searchCriteria.pageSize = pageSize;
            return this;
        }

        public SearchCriteria build() {
            return this.searchCriteria;
        }

    }

}
