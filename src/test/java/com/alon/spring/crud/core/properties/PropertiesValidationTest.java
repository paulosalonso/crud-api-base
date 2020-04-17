package com.alon.spring.crud.core.properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;

public class PropertiesValidationTest {

    private static final String CACHE_SCOPE_ERROR_MESSAGE =
            "Only one of the http cache scopes must be activated: public or private";

    private static final String NO_STORE_SCOPE_ERROR_MESSAGE =
            "It is not possible to enable the '%s' directive when the 'noStore' directive is enabled.";

    private Properties properties;

    @Before
    public void setUp() {
        properties = new Properties();
    }

    @Test
    public void whenIsCachePublicThenThrowsExceptionSettingCachePrivate() {
        properties.cacheControl.setCachePublic(true);
        assertThatThrownBy(() -> properties.cacheControl.setCachePrivate(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(CACHE_SCOPE_ERROR_MESSAGE);
    }

    @Test
    public void whenIsCachePrivateThenThrowsExceptionSettingCachePublic() {
        properties.cacheControl.setCachePrivate(true);
        assertThatThrownBy(() -> properties.cacheControl.setCachePublic(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(CACHE_SCOPE_ERROR_MESSAGE);
    }

    @Test
    public void whenIsNoStoreThenThrowsExceptionSettingCachePublic() {
        properties.cacheControl.setNoStore(true);
        assertThatThrownBy(() -> properties.cacheControl.setCachePublic(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(String.format(NO_STORE_SCOPE_ERROR_MESSAGE, "public"));
    }

    @Test
    public void whenIsCachePublicThenThrowsExceptionSettingNoStore() {
        properties.cacheControl.setCachePublic(true);
        assertThatThrownBy(() -> properties.cacheControl.setNoStore(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(String.format(NO_STORE_SCOPE_ERROR_MESSAGE, "public"));
    }

    @Test
    public void whenIsNoStoreThenThrowsExceptionSettingCachePrivate() {
        properties.cacheControl.setNoStore(true);
        assertThatThrownBy(() -> properties.cacheControl.setCachePrivate(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(String.format(NO_STORE_SCOPE_ERROR_MESSAGE, "private"));
    }

    @Test
    public void whenIsCachePrivateThenThrowsExceptionSettingNoStore() {
        properties.cacheControl.setCachePrivate(true);
        assertThatThrownBy(() -> properties.cacheControl.setNoStore(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(String.format(NO_STORE_SCOPE_ERROR_MESSAGE, "private"));
    }

    @Test
    public void whenIsNoStoreThenThrowsExceptionSettingNoCache() {
        properties.cacheControl.setNoStore(true);
        assertThatThrownBy(() -> properties.cacheControl.setNoCache(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(String.format(NO_STORE_SCOPE_ERROR_MESSAGE, "noCache"));
    }

    @Test
    public void whenIsNoCacheThenThrowsExceptionSettingNoStore() {
        properties.cacheControl.setNoCache(true);
        assertThatThrownBy(() -> properties.cacheControl.setNoStore(true))
                .isExactlyInstanceOf(CacheControlInvalidConfigurationException.class)
                .hasMessage(String.format(NO_STORE_SCOPE_ERROR_MESSAGE, "noCache"));
    }

}
