package com.alon.spring.crud.api.controller.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;

public class OutputPageTest {

    @Mock
    public Page page;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void whenBuildThenSuccess() {
        OutputPage outputPage = OutputPage.of()
                .page(1)
                .pageSize(10)
                .totalPages(1)
                .totalSize(10)
                .content(List.of("element"))
                .build();

        assertThat(outputPage).isNotNull();
        assertThat(outputPage.getPage()).isEqualTo(1);
        assertThat(outputPage.getPageSize()).isEqualTo(10);
        assertThat(outputPage.getTotalPages()).isEqualTo(1);
        assertThat(outputPage.getTotalSize()).isEqualTo(10);
        assertThat(outputPage.getContent())
                .hasSize(1)
                .first()
                .isEqualTo("element");
    }
}
