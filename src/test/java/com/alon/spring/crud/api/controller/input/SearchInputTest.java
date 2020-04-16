package com.alon.spring.crud.api.controller.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

public class SearchInputTest {

    @Test
    public void whenCreateSearchInputThenSuccess() {
        SearchInput searchInput = buildSearchInput();

        searchInput.setExpression("expression");

        assertThat(searchInput.getExpression()).isEqualTo("expression");
        assertThat(searchInput.expressionPresent()).isTrue();
        assertThat(searchInput.expressionNotPresent()).isFalse();
    }

    @Test
    public void whenCreateSearchInputWithoutExpressionThenSuccess() {
        SearchInput searchInput = buildSearchInput();

        assertThat(searchInput.getExpression()).isNull();
        assertThat(searchInput.expressionPresent()).isFalse();
        assertThat(searchInput.expressionNotPresent()).isTrue();
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
