package com.alon.spring.crud.service;

import java.util.List;

public enum SearchType {

    NONE(""),
    FILTER("F"),
    FILTER_ORDER("FO"),
    FILTER_EXPAND("FE"),
    FILTER_ORDER_EXPAND("FOE"),
    ORDER("O"),
    ORDER_EXPAND("OE"),
    EXPAND("E");

    private static final List<SearchType> options = List.of(SearchType.values());

    private String option;

    private SearchType(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }

    public static SearchType getByOptionString(String option) {

        return options.stream()
                      .filter(searchOption -> searchOption.option.equals(option))
                      .findFirst()
                      .orElseThrow(IllegalArgumentException::new);

    }

}
