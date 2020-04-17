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

        assertThat(properties.cacheControl)
                .isNotNull()
                .satisfies(this::assertCacheControl);

        assertThat(properties.projection)
                .isNotNull()
                .satisfies(this::assertProjection);

        assertThat(properties.search)
                .isNotNull()
                .satisfies(this::assertSearch);
    }

    private void assertCacheControl(Properties.CacheControlProperties cacheControl) {
        assertThat(cacheControl.noStore).isFalse();
        assertThat(cacheControl.cachePrivate).isTrue();
        assertThat(cacheControl.cachePublic).isFalse();
        assertThat(cacheControl.noCache).isTrue();
        assertThat(cacheControl.maxAge).isEqualTo(500);
    }

    private void assertProjection(Properties.ProjectionProperties projection) {
        assertThat(projection.useDefaultIfError).isFalse();
    }

    private void assertSearch(Properties.SearchProperties properties) {
        assertThat(properties.enableExpressionFilter).isTrue();
    }

}
