package com.alon.spring.crud.api.controller.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

public class SearchInputTest {

    @Test
    public void whenCreateSearchInputThenSuccess() {
        SearchInput searchInput = buildSearchInput();

        searchInput.setFilter("expression");

        assertThat(searchInput.getFilter()).isEqualTo("expression");
        assertThat(searchInput.filterPresent()).isTrue();
        assertThat(searchInput.filterNotPresent()).isFalse();
    }

    @Test
    public void whenCreateSearchInputWithoutExpressionThenSuccess() {
        SearchInput searchInput = buildSearchInput();

        assertThat(searchInput.getFilter()).isNull();
        assertThat(searchInput.filterPresent()).isFalse();
        assertThat(searchInput.filterNotPresent()).isTrue();
    }

    private SearchInput buildSearchInput() {
        return new SearchInput() {
            @Override
            public Specification toSpecification() {
                return null;
            }
        };
    }

}
