package com.alon.spring.crud.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.alon.spring.crud")
public class Properties {

    public CacheControlProperties cacheControl = new CacheControlProperties();
    public Search search = new Search();

    public CacheControlProperties getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(CacheControlProperties cacheControl) {
        this.cacheControl = cacheControl;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public class CacheControlProperties {
        public long maxAge = 600;
        public boolean cachePrivate;
        public boolean cachePublic;
        public boolean noCache;
        public boolean noStore;

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }

        public boolean isCachePrivate() {
            return cachePrivate;
        }

        public void setCachePrivate(boolean cachePrivate) {
            validateCacheScope(cachePublic, cachePrivate);
            validateNoStore(noStore);
            this.cachePrivate = cachePrivate;
        }

        public boolean isCachePublic() {
            return cachePublic;
        }

        public void setCachePublic(boolean cachePublic) {
            validateCacheScope(cachePublic, cachePrivate);
            validateNoStore(noStore);
            this.cachePublic = cachePublic;
        }

        public boolean isNoCache() {
            return noCache;
        }

        public void setNoCache(boolean noCache) {
            validateNoStore(noStore);
            this.noCache = noCache;
        }

        public boolean isNoStore() {
            return noStore;
        }

        public void setNoStore(boolean noStore) {
            validateNoStore(noStore);
            this.noStore = noStore;
        }

        private void validateCacheScope(boolean cachePublic, boolean cachePrivate) {
            if (cachePublic && cachePrivate)
                throw new CacheControlInvalidConfigurationException(
                        "Only one of the http cache scopes must be activated: public or private");
        }

        private void validateNoStore(boolean noStore) {
            if (noStore) {
                String message = "It is not possible to enable the '%s' directive when the 'noStore' directive is enabled.";

                if (cachePublic)
                    throw new CacheControlInvalidConfigurationException(String.format(message, "public"));

                if (cachePrivate)
                    throw new CacheControlInvalidConfigurationException(String.format(message, "private"));

                if (noCache)
                    throw new CacheControlInvalidConfigurationException(String.format(message, "noCache"));
            }
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
