package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleRepository;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import com.alon.spring.crud.domain.service.exception.ReadException;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.AFTER_READ;
import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.BEFORE_READ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


public class ExampleServiceReadTest {

    @InjectMocks
    private ExampleService service;

    @Mock
    private ExampleRepository repository;
    
    @Mock
    private Function<Long, Long> beforeReadHookA;

    @Mock
    private Function<Long, Long> beforeReadHookB;

    @Mock
    private Function<Example, Example> afterReadHookA;

    @Mock
    private Function<Example, Example> afterReadHookB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service.clearHooks(BEFORE_READ, AFTER_READ);
    }

    @Test
    public void whenSimpleReadThenFindById() {
        Example entity = buildEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Example result = service.read(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStringProperty()).isEqualTo("string");

        verify(repository).findById(1L);
    }

    @Test
    public void whenReadWithEmptyExpandListThenFindById() {
        Example entity = buildEntity();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Example result = service.read(1L, Collections.emptyList());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStringProperty()).isEqualTo("string");

        verify(repository).findById(1L);
        verify(repository, never()).findById(eq(1L), any());
    }

    @Test
    public void whenReadWithExpandThenFindById() {
        Example entity = buildEntity();
        List<String> expand = List.of("property");
        EntityGraph entityGraph = new DynamicEntityGraph(expand);

        when(repository.findById(1L, entityGraph)).thenReturn(Optional.of(entity));

        Example result = service.read(1L, expand);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStringProperty()).isEqualTo("string");

        verify(repository).findById(1L, entityGraph);
    }

    @Test
    public void whenReadNonExistentItThenThrowsNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.read(1L))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("ID not found -> 1");

        verify(repository).findById(1L);
    }

    @Test
    public void whenReadWithHooksThenExecuteHooks() {
        addHooks();

        Example entity = buildEntity();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(beforeReadHookA.apply(1L)).thenReturn(1L);
        when(beforeReadHookB.apply(1L)).thenReturn(1L);
        when(afterReadHookA.apply(entity)).thenReturn(entity);
        when(afterReadHookB.apply(entity)).thenReturn(entity);

        Example result = service.read(1L);

        verify(repository).findById(1L);

        InOrder inOrder = inOrder(beforeReadHookA, beforeReadHookB, afterReadHookA, afterReadHookB);
        inOrder.verify(beforeReadHookA).apply(1L);
        inOrder.verify(beforeReadHookB).apply(1L);
        inOrder.verify(afterReadHookA).apply(result);
        inOrder.verify(afterReadHookB).apply(result);
    }

    @Test
    public void whenBeforeReadHookThrowsExceptionThenThrowReadException() {
        addHooks();

        when(beforeReadHookA.apply(1L)).thenThrow(new RuntimeException("Before read error"));

        assertThatThrownBy(() -> service.read(1L))
                .isExactlyInstanceOf(ReadException.class)
                .hasMessage("Error reading entity: Before read error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(beforeReadHookA).apply(1L);
        verifyZeroInteractions(beforeReadHookB);
        verifyZeroInteractions(afterReadHookA);
        verifyZeroInteractions(afterReadHookB);
        verifyZeroInteractions(repository);
    }

    @Test
    public void whenAfterReadHookThrowsExceptionThenThrowReadException() {
        addHooks();

        Example entity = buildEntity();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(beforeReadHookA.apply(1L)).thenReturn(1L);
        when(beforeReadHookB.apply(1L)).thenReturn(1L);
        when(afterReadHookA.apply(entity)).thenThrow(new RuntimeException("After read error"));

        assertThatThrownBy(() -> service.read(1L))
                .isExactlyInstanceOf(ReadException.class)
                .hasMessage("Error reading entity: After read error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(afterReadHookA).apply(entity);
        verifyZeroInteractions(afterReadHookB);
        verify(repository).findById(1L);
    }

    private Example buildEntity() {
        Example entity = new Example();
        entity.setId(1L);
        entity.setStringProperty("string");
        return entity;
    }

    private void addHooks() {
        service.addBeforeReadHook(beforeReadHookA);
        service.addBeforeReadHook(beforeReadHookB);
        service.addAfterReadHook(afterReadHookA);
        service.addAfterReadHook(afterReadHookB);

        reset(beforeReadHookA, beforeReadHookB, afterReadHookA, afterReadHookB);
    }
}
