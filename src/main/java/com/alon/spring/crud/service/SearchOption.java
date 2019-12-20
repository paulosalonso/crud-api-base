package com.alon.spring.crud.service;

import java.util.List;
import java.util.stream.Stream;

public enum SearchOption {

    NONE(""),
    SPECIFICATION("S"),
    SPECIFICATION_ORDER("SO"),
    SPECIFICATION_EXPAND("SE"),
    SPECIFICATION_ORDER_EXPAND("SOE"),
    ORDER("O"),
    ORDER_EXPAND("OE"),
    EXPAND("E");

    private static final List<SearchOption> options = List.of(SearchOption.values());

    private String option;

    private SearchOption(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }

    public static SearchOption getByOptionString(String option) {

        return options.stream()
                      .filter(searchOption -> searchOption.option.equals(option))
                      .findFirst()
                      .orElseThrow(IllegalArgumentException::new);

    }

}
