package com.alon.spring.crud.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("crudProperties")
@ConfigurationProperties(prefix = "com.alon")
public class Properties {

    public CacheControlProperties cacheControl = new CacheControlProperties();
    public SearchProperties search = new SearchProperties();
    public ProjectionProperties projection = new ProjectionProperties();
    public SerializationProperties serialization = new SerializationProperties();

    public CacheControlProperties getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(CacheControlProperties cacheControl) {
        this.cacheControl = cacheControl;
    }

    public SearchProperties getSearch() {
        return search;
    }

    public void setSearch(SearchProperties search) {
        this.search = search;
    }

    public ProjectionProperties getProjection() {
        return projection;
    }

    public void setProjection(ProjectionProperties projection) {
        this.projection = projection;
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
            this.cachePrivate = cachePrivate;
            validateCacheScope();
            validateNoStore();
        }

        public boolean isCachePublic() {
            return cachePublic;
        }

        public void setCachePublic(boolean cachePublic) {
            this.cachePublic = cachePublic;
            validateCacheScope();
            validateNoStore();
        }

        public boolean isNoCache() {
            return noCache;
        }

        public void setNoCache(boolean noCache) {
            this.noCache = noCache;
            validateNoStore();
        }

        public boolean isNoStore() {
            return noStore;
        }

        public void setNoStore(boolean noStore) {
            this.noStore = noStore;
            validateNoStore();
        }

        private void validateCacheScope() {
            if (cachePublic && cachePrivate)
                throw new CacheControlInvalidConfigurationException(
                        "Only one of the http cache scopes must be activated: public or private");
        }

        private void validateNoStore() {
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

    public class SearchProperties {
        public boolean enableExpressionFilter = false;

        public boolean isEnableExpressionFilter() {
            return enableExpressionFilter;
        }

        public void setEnableExpressionFilter(boolean enableExpressionFilter) {
            this.enableExpressionFilter = enableExpressionFilter;
        }
    }

    public class ProjectionProperties {
        public boolean useDefaultIfError = true;

        public boolean isUseDefaultIfError() {
            return useDefaultIfError;
        }

        public void setUseDefaultIfError(boolean useDefaultIfError) {
            this.useDefaultIfError = useDefaultIfError;
        }
    }

    public class SerializationProperties {
        public boolean forceLazyLoading = false;
        public boolean serializeIdentifierForLazyNotLoadedObjects = true;
        public boolean writeDatesAsTimestamps = false;
        public boolean includeNullValues = false;

        public boolean isForceLazyLoading() {
            return forceLazyLoading;
        }

        public void setForceLazyLoading(boolean forceLazyLoading) {
            this.forceLazyLoading = forceLazyLoading;
        }

        public boolean isSerializeIdentifierForLazyNotLoadedObjects() {
            return serializeIdentifierForLazyNotLoadedObjects;
        }

        public void setSerializeIdentifierForLazyNotLoadedObjects(boolean serializeIdentifierForLazyNotLoadedObjects) {
            this.serializeIdentifierForLazyNotLoadedObjects = serializeIdentifierForLazyNotLoadedObjects;
        }

        public boolean isWriteDatesAsTimestamps() {
            return writeDatesAsTimestamps;
        }

        public void setWriteDatesAsTimestamps(boolean writeDatesAsTimestamps) {
            this.writeDatesAsTimestamps = writeDatesAsTimestamps;
        }

        public boolean isIncludeNullValues() {
            return includeNullValues;
        }

        public void setIncludeNullValues(boolean includeNullValues) {
            this.includeNullValues = includeNullValues;
        }
    }
}
