package com.alon.spring.crud.api.controller.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class OptionsTest {

    @Test
    public void whenNewInstanceThenSuccess() {
        Options options = new Options();
        options.setExpand(List.of("property"));
        options.setProjection("projection");

        assertThat(options.getExpand())
                .isNotNull()
                .hasSize(1)
                .first()
                .isEqualTo("property");
        assertThat(options.getProjection())
                .isNotNull()
                .isEqualTo("projection");
    }
}
