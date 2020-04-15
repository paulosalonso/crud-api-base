package com.alon.spring.crud.domain.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchCriteriaTest {

    @Test
    public void whenBuildWithSpecificationFilterThenSuccess() {
        SearchCriteria searchCriteria = SearchCriteria.of()
                .pageable(PageRequest.of(1, 100))
                .expand(List.of("property"))
                .filter(((root, query, criteriaBuilder) -> null))
                .build();

        assertThat(searchCriteria.getPageable()).isNotNull();
        assertThat(searchCriteria.getFilter()).isNotNull();
        assertThat(searchCriteria.getSearchOption()).isEqualTo(SearchType.FILTER_EXPAND);
        assertThat(searchCriteria.getExpand())
                .isNotNull()
                .satisfies(entityGraph -> {
                    assertThat(entityGraph.getEntityGraphAttributePaths())
                            .hasSize(1)
                            .first()
                            .isEqualTo("property");
                });
    }

    @Test
    public void whenBuildWithStringFilterThenSuccess() {
        SearchCriteria searchCriteria = SearchCriteria.of()
                .filter("name:name")
                .build();

        assertThat(searchCriteria.getFilter()).isNotNull();
    }

    @Test
    public void whenBuildWithoutFilterAndExpandThenReturnNoneOption() {
        SearchCriteria searchCriteria = SearchCriteria.of()
                .pageable(PageRequest.of(1, 100))
                .build();

        assertThat(searchCriteria.getSearchOption()).isEqualTo(SearchType.NONE);
    }

    @Test
    public void whenBuildWithNullOrEmptyExpandListThenReturnNullExpand() {
        SearchCriteria searchCriteria = SearchCriteria.of()
                .expand(null)
                .build();

        assertThat(searchCriteria.getExpand()).isNull();

        searchCriteria = SearchCriteria.of()
                .expand(Collections.emptyList())
                .build();

        assertThat(searchCriteria.getExpand()).isNull();
    }

    @Test
    public void whenBuildWithoutFilterThenReturnExpandOption() {
        SearchCriteria searchCriteria = SearchCriteria.of()
                .pageable(PageRequest.of(1, 100))
                .expand(List.of("property"))
                .build();

        assertThat(searchCriteria.getSearchOption()).isEqualTo(SearchType.EXPAND);
    }

    @Test
    public void whenBuildWithoutExpandThenReturnFilterOption() {
        SearchCriteria searchCriteria = SearchCriteria.of()
                .pageable(PageRequest.of(1, 100))
                .filter("name:name")
                .build();

        assertThat(searchCriteria.getSearchOption()).isEqualTo(SearchType.FILTER);
    }

}
