package com.alon.spring.crud.domain.service;

import com.alon.spring.specification.ExpressionSpecification;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SearchCriteria {

    private Specification filter;
    private Pageable pageable;
    private EntityGraph expand;

    public Specification getFilter() {
        return filter;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public EntityGraph getExpand() {
        return expand;
    }
    public SearchType getSearchOption() {

        String option = "";

        if (this.filter != null)
            option += SearchType.FILTER.getOption();

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

        public SearchCriteriaBuilder pageable(Pageable pageable) {
            this.searchCriteria.pageable = pageable;
            return this;
        }

        public SearchCriteriaBuilder expand(List<String> expand) {
            if (expand != null && !expand.isEmpty())
                this.searchCriteria.expand = new DynamicEntityGraph(expand);

            return this;
        }
        public SearchCriteria build() {
            return this.searchCriteria;
        }

    }

}
