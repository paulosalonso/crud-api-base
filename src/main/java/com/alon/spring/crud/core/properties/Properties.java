package com.alon.spring.crud.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("com.alon.spring.crud")
public class Properties {

    public CacheControl cacheControl = new CacheControl();
    public Search search = new Search();

    public CacheControl getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(CacheControl cacheControl) {
        this.cacheControl = cacheControl;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public class CacheControl {
        public long maxAge = 60;

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    public class Search {
        public boolean enableExpressionFilter = false;

        public boolean isEnableExpressionFilter() {
            return enableExpressionFilter;
        }

        public void setEnableExpressionFilter(boolean enableExpressionFilter) {
            this.enableExpressionFilter = enableExpressionFilter;
        }
    }
}
