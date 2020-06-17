package com.alon.spring.crud.api.controller.input;

import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.specification.ExpressionSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SearchResolver {

    private Properties properties;

    public SearchResolver(Properties properties) {
        this.properties = properties;
    }

    public <SEARCH_INPUT_TYPE extends SearchInput> Specification resolve(SEARCH_INPUT_TYPE search) {
        if (search.filterPresent()) {
            if (!properties.search.enableExpressionFilter)
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "The filter by expression feature is not enabled.");

            return ExpressionSpecification.of(search.getFilter());
        }

        return search.toSpecification();
    }
}
