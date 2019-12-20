package com.alon.spring.crud.service;

import com.alon.querydecoder.SingleExpression;
import com.alon.querydecoder.SingleExpressionParser;
import com.alon.spring.crud.repository.specification.SpringJpaSpecification;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SearchCriteria {

    private Specification filter;
    private SingleExpression order;
    private EntityGraph expand;
    private int page;
    private int size;

    public Specification getFilter() {
        return filter;
    }

    public SingleExpression getOrder() {
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

    public SearchOption getSearchOption() {

        String option = "";

        if (this.filter != null)
            option += SearchOption.SPECIFICATION.getOption();

        if (this.order != null)
            option += SearchOption.ORDER.getOption();

        if (this.expand != null)
            option += SearchOption.EXPAND.getOption();

        return SearchOption.getByOptionString(option);

    }

    public static SearchCriteriaBuilder of() {
        return new SearchCriteriaBuilder();
    }

    public static final class SearchCriteriaBuilder {

        private SearchCriteria searchCriteria;

        private SearchCriteriaBuilder() {
            this.searchCriteria = new SearchCriteria();
        }

        public SearchCriteriaBuilder filter(String filter) {
            if (filter != null)
                this.searchCriteria.filter = SpringJpaSpecification.of(filter);

            return this;
        }

        public SearchCriteriaBuilder order(String order) {
            if (order != null)
                this.searchCriteria.order = SingleExpressionParser.parse(order);

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
