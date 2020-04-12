package com.alon.spring.crud.api.controller.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;

import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.model.EntityTest;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

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

    @Test
    public void whenBuildFromPageThenSuccess() {
        mockPage(List.of("element"));

        OutputPage outputPage = OutputPage.of(page);

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

    @Test
    public void whenBuildFromPageWithProjectorThenSuccess() {
        EntityTest entity = new EntityTest();
        entity.setId(1L);
        entity.setStringProperty("element");

        mockPage(List.of(entity));

        OutputPage outputPage = OutputPage.of(page, new TestProjector());

        assertThat(outputPage).isNotNull();
        assertThat(outputPage.getPage()).isEqualTo(1);
        assertThat(outputPage.getPageSize()).isEqualTo(10);
        assertThat(outputPage.getTotalPages()).isEqualTo(1);
        assertThat(outputPage.getTotalSize()).isEqualTo(10);
        assertThat(outputPage.getContent())
                .hasSize(1)
                .first()
                .satisfies(content -> {
                   assertThat(content).isNotNull();
                   assertThat(((EntityTest) content).getId()).isEqualTo(1L);
                   assertThat(((EntityTest) content).getStringProperty()).isEqualTo("element-projected");
                });
    }

    private void mockPage(List<Object> content) {
        when(page.getNumber()).thenReturn(1);
        when(page.getNumberOfElements()).thenReturn(10);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(10L);
        when(page.getContent()).thenReturn(content);
    }

    private void assertOutputPage(OutputPage outputPage, String expectedContent) {
        assertThat(outputPage).isNotNull();
        assertThat(outputPage.getPage()).isEqualTo(1);
        assertThat(outputPage.getPageSize()).isEqualTo(10);
        assertThat(outputPage.getTotalPages()).isEqualTo(1);
        assertThat(outputPage.getTotalSize()).isEqualTo(10);
        assertThat(outputPage.getContent())
                .hasSize(1)
                .first()
                .isEqualTo(expectedContent);
    }

    private class TestProjector implements Projector<EntityTest, EntityTest> {
        @Override
        public EntityTest project(EntityTest input) throws ProjectionException {
            input.setStringProperty(input.getStringProperty().concat("-projected"));
            return input;
        }
    }
}
