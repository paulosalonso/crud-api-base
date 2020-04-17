package com.alon.spring.crud.core.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(value = Properties.class)
@TestPropertySource("classpath:properties-test.properties")
public class CustomPropertiesTest {

    @Autowired
    private Properties properties;

    @Test
    public void whenThereArePropertiesDeclaredThenLoadThem() {
        assertThat(properties).isNotNull();

        assertThat(properties.getCacheControl())
                .isNotNull()
                .satisfies(this::assertCacheControl);

        assertThat(properties.getProjection())
                .isNotNull()
                .satisfies(this::assertProjection);

        assertThat(properties.getSearch())
                .isNotNull()
                .satisfies(this::assertSearch);
    }

    private void assertCacheControl(Properties.CacheControlProperties cacheControl) {
        assertThat(cacheControl.isNoStore()).isFalse();
        assertThat(cacheControl.isCachePrivate()).isTrue();
        assertThat(cacheControl.isCachePublic()).isFalse();
        assertThat(cacheControl.isNoCache()).isTrue();
        assertThat(cacheControl.getMaxAge()).isEqualTo(500);
    }

    private void assertProjection(Properties.ProjectionProperties projection) {
        assertThat(projection.isUseDefaultIfError()).isFalse();
    }

    private void assertSearch(Properties.SearchProperties properties) {
        assertThat(properties.isEnableExpressionFilter()).isTrue();
    }

}
