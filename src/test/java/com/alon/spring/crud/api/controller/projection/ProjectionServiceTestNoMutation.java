package com.alon.spring.crud.api.controller.projection;

import static com.alon.spring.crud.api.projection.ProjectionService.NOP_PROJECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.alon.spring.crud.api.controller.output.ExampleDTO;
import com.alon.spring.crud.api.controller.output.OutputPage;
import com.alon.spring.crud.api.projection.ProjectionRepresentation;
import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProjectionServiceTestNoMutation {

    @Autowired
    private ProjectionService projectionService;

    @Test
    public void whenProjectNOPProjectionThenReturnSameObject() {
        Example example = Example.of()
                .id(1L)
                .stringProperty("property")
                .build();

        Example projected = projectionService.project(NOP_PROJECTION, example);

        assertThat(projected).isSameAs(example);
    }

    @Test
    public void whenProjectWithExistentProjectionThenReturnProjectedObject() {
        Example example = Example.of()
                .id(1L)
                .stringProperty("property")
                .build();

        ExampleDTO projected = projectionService.project("exampleProjection", example);

        assertThat(projected).isNotNull();
        assertThat(projected.getId()).isEqualTo(example.getId());
        assertThat(projected.getProperty()).isEqualTo(example.getStringProperty());
    }

    @Test
    public void whenProjectWithNonExistentProjectionThenThrowsProjectionException() {
        Example example = Example.of()
                .id(1L)
                .stringProperty("property")
                .build();

        assertThatThrownBy(() -> projectionService.project("nonExistentProjection", example))
                .isExactlyInstanceOf(ProjectionException.class)
                .hasMessage("Projection 'nonExistentProjection' not found")
                .hasNoCause();
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void whenProjectorThrowsExceptionThenThrowsProjectionException() {
        Projector brokerProjector = mockBrokenProjector();
        ReflectionTestUtils.setField(projectionService,
                "projections", Map.of("brokenProjection", brokerProjector));

        Example example = Example.of()
                .id(1L)
                .stringProperty("property")
                .build();

        assertThatThrownBy(() -> projectionService.project("brokenProjection", example))
                .isExactlyInstanceOf(ProjectionException.class)
                .hasMessage("Error projecting entity Example with projector 'brokenProjection'")
                .hasCauseExactlyInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("Mocked exception");
    }

    @Test
    public void whenProjectPageWithNOPProjectionThenReturnOutputPageWithSameObjects() {
        OutputPage<Example> outputPage = projectionService.project(NOP_PROJECTION, mockPage());

        assertThat(outputPage).isNotNull();
        assertThat(outputPage.getPage()).isEqualTo(1);
        assertThat(outputPage.getPageSize()).isEqualTo(2);
        assertThat(outputPage.getTotalPages()).isEqualTo(1);
        assertThat(outputPage.getTotalSize()).isEqualTo(2);
        assertThat(outputPage.getContent())
                .hasSize(2)
                .satisfies(content -> {
                    assertThat(content.get(0)).satisfies(entity -> {
                        assertThat(entity.getId()).isEqualTo(1L);
                        assertThat(entity.getStringProperty()).isEqualTo("property 1");
                    });
                    assertThat(content.get(1)).satisfies(entity -> {
                        assertThat(entity.getId()).isEqualTo(2L);
                        assertThat(entity.getStringProperty()).isEqualTo("property 2");
                    });
                });
    }

    @Test
    public void whenProjectPageWithExistentProjectionThenReturnOutputPageWithProjectedObjects() {
        OutputPage<ExampleDTO> outputPage =
                projectionService.project("exampleProjection", mockPage());

        assertThat(outputPage).isNotNull();
        assertThat(outputPage.getPage()).isEqualTo(1);
        assertThat(outputPage.getPageSize()).isEqualTo(2);
        assertThat(outputPage.getTotalPages()).isEqualTo(1);
        assertThat(outputPage.getTotalSize()).isEqualTo(2);
        assertThat(outputPage.getContent())
                .hasSize(2)
                .satisfies(content -> {
                    assertThat(content.get(0)).satisfies(entity -> {
                        assertThat(entity.getId()).isEqualTo(1L);
                        assertThat(entity.getProperty()).isEqualTo("property 1");
                    });
                    assertThat(content.get(1)).satisfies(entity -> {
                        assertThat(entity.getId()).isEqualTo(2L);
                        assertThat(entity.getProperty()).isEqualTo("property 2");
                    });
                });
    }

    @Test
    public void whenProjectPageWithNonExistentProjectionThenThrowsProjectionException() {
        assertThatThrownBy(() -> projectionService.project("nonExistentProjection", mockPage()))
                .isExactlyInstanceOf(ProjectionException.class)
                .hasMessage("Projection 'nonExistentProjection' not found")
                .hasNoCause();
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void whenProjectorThrowsExceptionProjectingPageThenThrowsProjectionException() {
        Projector brokerProjector = mockBrokenProjector();
        ReflectionTestUtils.setField(projectionService,
                "projections", Map.of("brokenProjection", brokerProjector));

        assertThatThrownBy(() -> projectionService.project("brokenProjection", mockPage()))
                .isExactlyInstanceOf(ProjectionException.class)
                .hasMessage("Error projecting page with projector 'brokenProjection'")
                .hasCauseExactlyInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("Mocked exception");
    }

    @Test
    public void whenGetRequiredExpandFromNonExistentProjectionThenThrowsProjectionException() {
        assertThatThrownBy(() -> projectionService.getRequiredExpand("nonExistentProjection"))
                .isExactlyInstanceOf(ProjectionException.class)
                .hasMessage("Projection 'nonExistentProjection' not found")
                .hasNoCause();
    }

    @Test
    public void whenCheckIfProjectionExistsThenReturnTrue() {
        assertTrue(projectionService.projectionExists("exampleProjection"));
    }

    @Test
    public void whenCheckIfProjectionExistsThenReturnFalse() {
        assertFalse(projectionService.projectionExists("nonExistentProjection"));
    }

    @Test
    public void whenGetRepresentationsThenReturn() {
        Map<Class, List<ProjectionRepresentation>> representationsCache = spy(new HashMap<>());
        ReflectionTestUtils.setField(projectionService, "representationsCache", representationsCache);

        List<ProjectionRepresentation> representations =
                projectionService.getEntityRepresentations(Example.class, () -> "default", () -> "default");

        assertRepresentations(representations);

        verify(representationsCache, never()).get(Example.class);
        verify(representationsCache).put(Example.class, representations);

        representations = projectionService.getEntityRepresentations(Example.class, () -> "default", () -> "default");

        assertRepresentations(representations);

        verify(representationsCache).get(Example.class);
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void whenProjectorGenericsNotDefinedInClassDeclarationThenDoNotReturnIt() {
        ReflectionTestUtils.setField(projectionService,
                "projections", Map.of("projector", new GenericProjectorExample<Example, Example>()));

        List<ProjectionRepresentation> representations =
                projectionService.getEntityRepresentations(Example.class, () -> "default", () -> "default");

        assertThat(representations).isNullOrEmpty();
    }

    private void assertRepresentations(List<ProjectionRepresentation> representations) {
        assertThat(representations)
                .hasSize(1)
                .first()
                .satisfies(representation -> {
                    assertThat(representation.getProjectionName()).isEqualTo("exampleProjection");
                    assertThat(representation.getRepresentation())
                            .hasSize(2)
                            .containsOnlyKeys("id", "property")
                            .containsValues("long", "string");
                });
    }

    private Projector mockBrokenProjector() {
        Projector projector = mock(Projector.class);
        when(projector.project(any())).thenThrow(new RuntimeException("Mocked exception"));
        return projector;
    }

    private Page mockPage() {
        Page page = mock(Page.class);
        when(page.getNumber()).thenReturn(1);
        when(page.getNumberOfElements()).thenReturn(2);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(2L);
        when(page.getContent()).thenReturn(List.of(
                Example.of()
                        .id(1L)
                        .stringProperty("property 1")
                        .build(),
                Example.of()
                        .id(2L)
                        .stringProperty("property 2")
                        .build()));

        return page;
    }

}
