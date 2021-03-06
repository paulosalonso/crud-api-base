package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleCrudRepository;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import com.alon.spring.crud.domain.service.exception.UpdateException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Function;

import static com.alon.spring.crud.domain.service.LifeCycleHook.AFTER_CREATE;
import static com.alon.spring.crud.domain.service.LifeCycleHook.BEFORE_CREATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


public class ExampleServiceUpdateTest {

    @InjectMocks
    private ExampleService service;

    @Mock
    private ExampleCrudRepository repository;
    
    @Mock
    private Function<Example, Example> beforeUpdateHookA;

    @Mock
    private Function<Example, Example> beforeUpdateHookB;

    @Mock
    private Function<Example, Example> afterUpdateHookA;

    @Mock
    private Function<Example, Example> afterUpdateHookB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service.clearHooks(BEFORE_CREATE, AFTER_CREATE);
    }

    @Test
    public void whenSimpleUpdateThenReturnUpdated() {
        Example toUpdate = buildEntityWithId(1L);

        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(toUpdate)).thenReturn(toUpdate);

        Example result = service.update(toUpdate);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStringProperty()).isEqualTo("string");

        verify(repository).existsById(1L);
        verify(repository).save(toUpdate);
    }

    @Test
    public void whenUpdateNonExistentEntityThenThrowsNotFountException() {
        Example toUpdate = buildEntityWithId(1L);

        when(repository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.update(toUpdate))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Entity to update not found")
                .hasNoCause();

        verify(repository).existsById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    public void whenRepositoryThrowsExceptionThenThrowUpdateException() {
        Example toUpdate = buildEntityWithId(1L);

        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(toUpdate)).thenThrow(new RuntimeException("Error updating"));

        assertThatThrownBy(() -> service.update(toUpdate))
                .isExactlyInstanceOf(UpdateException.class)
                .hasMessage("Error updating")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(repository).existsById(1L);
        verify(repository).save(toUpdate);
    }

    @Test
    public void whenCreateWithHooksThenExecuteHooks() {
        addHooks();

        Example toUpdate = buildEntityWithId(1L);

        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(toUpdate)).thenReturn(toUpdate);
        when(beforeUpdateHookA.apply(toUpdate)).thenReturn(toUpdate);
        when(beforeUpdateHookB.apply(toUpdate)).thenReturn(toUpdate);
        when(afterUpdateHookA.apply(toUpdate)).thenReturn(toUpdate);
        when(afterUpdateHookB.apply(toUpdate)).thenReturn(toUpdate);

        service.update(toUpdate);

        verify(repository).existsById(1L);
        verify(repository).save(toUpdate);

        InOrder inOrder = inOrder(beforeUpdateHookA, beforeUpdateHookB, afterUpdateHookA, afterUpdateHookB);
        inOrder.verify(beforeUpdateHookA).apply(toUpdate);
        inOrder.verify(beforeUpdateHookB).apply(toUpdate);
        inOrder.verify(afterUpdateHookA).apply(toUpdate);
        inOrder.verify(afterUpdateHookB).apply(toUpdate);
    }

    @Test
    public void whenBeforeUpdateHookThrowsExceptionThenThrowUpdateException() {
        addHooks();

        Example toUpdate = buildEntityWithId(1L);

        when(repository.existsById(1L)).thenReturn(true);
        when(beforeUpdateHookA.apply(toUpdate)).thenThrow(new RuntimeException("Before update error"));

        assertThatThrownBy(() -> service.update(toUpdate))
                .isExactlyInstanceOf(UpdateException.class)
                .hasMessage("Before update error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(beforeUpdateHookA).apply(toUpdate);
        verifyZeroInteractions(beforeUpdateHookB);
        verifyZeroInteractions(afterUpdateHookA);
        verifyZeroInteractions(afterUpdateHookB);
        verify(repository).existsById(1L);
        verifyZeroInteractions(repository);
    }

    @Test
    public void whenAfterCreateHookThrowsExceptionThenThrowCreateException() {
        addHooks();

        Example toUpdate = buildEntityWithId(1L);

        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(toUpdate)).thenReturn(toUpdate);
        when(beforeUpdateHookA.apply(toUpdate)).thenReturn(toUpdate);
        when(beforeUpdateHookB.apply(toUpdate)).thenReturn(toUpdate);
        when(afterUpdateHookA.apply(toUpdate)).thenThrow(new RuntimeException("After update error"));

        assertThatThrownBy(() -> service.update(toUpdate))
                .isExactlyInstanceOf(UpdateException.class)
                .hasMessage("After update error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(repository).existsById(1L);
        verify(repository).save(toUpdate);
        verify(beforeUpdateHookA).apply(toUpdate);
        verify(beforeUpdateHookB).apply(toUpdate);
        verify(afterUpdateHookA).apply(toUpdate);
        verifyZeroInteractions(afterUpdateHookB);
    }

    private Example buildEntityWithId(Long id) {
        Example entity = new Example();
        entity.setId(id);
        entity.setStringProperty("string");
        return entity;
    }

    private void addHooks() {
        service.addBeforeUpdateHook(beforeUpdateHookA);
        service.addBeforeUpdateHook(beforeUpdateHookB);
        service.addAfterUpdateHook(afterUpdateHookA);
        service.addAfterUpdateHook(afterUpdateHookB);

        reset(beforeUpdateHookA, beforeUpdateHookB, afterUpdateHookA, afterUpdateHookB);
    }
}
