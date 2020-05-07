package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleCrudRepository;
import com.alon.spring.crud.domain.service.exception.DeleteException;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Function;

import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.AFTER_CREATE;
import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.BEFORE_CREATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


public class ExampleDeleteTest {

    @InjectMocks
    private ExampleService service;

    @Mock
    private ExampleCrudRepository repository;
    
    @Mock
    private Function<Long, Long> beforeDeleteHookA;

    @Mock
    private Function<Long, Long> beforeDeleteHookB;

    @Mock
    private Function<Long, Long> afterDeleteHookA;

    @Mock
    private Function<Long, Long> afterDeleteHookB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service.clearHooks(BEFORE_CREATE, AFTER_CREATE);
    }

    @Test
    public void whenSimpleDeleteThenSuccess() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    public void whenDeleteNonExistentEntityThenThrowsNotFountException() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(1L))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("ID not found -> 1")
                .hasNoCause();

        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    public void whenRepositoryThrowsExceptionThenThrowDeleteException() {
        when(repository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Error deleting")).when(repository).deleteById(1L);

        assertThatThrownBy(() -> service.delete(1L))
                .isExactlyInstanceOf(DeleteException.class)
                .hasMessage("Error deleting")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    public void whenDeleteWithHooksThenExecuteHooks() {
        addHooks();

        when(repository.existsById(1L)).thenReturn(true);
        when(beforeDeleteHookA.apply(1L)).thenReturn(1L);
        when(beforeDeleteHookB.apply(1L)).thenReturn(1L);
        when(afterDeleteHookA.apply(1L)).thenReturn(1L);
        when(afterDeleteHookB.apply(1L)).thenReturn(1L);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);

        InOrder inOrder = inOrder(beforeDeleteHookA, beforeDeleteHookB, afterDeleteHookA, afterDeleteHookB);
        inOrder.verify(beforeDeleteHookA).apply(1L);
        inOrder.verify(beforeDeleteHookB).apply(1L);
        inOrder.verify(afterDeleteHookA).apply(1L);
        inOrder.verify(afterDeleteHookB).apply(1L);
    }

    @Test
    public void whenBeforeUpdateHookThrowsExceptionThenThrowUpdateException() {
        addHooks();

        when(repository.existsById(1L)).thenReturn(true);
        when(beforeDeleteHookA.apply(1L)).thenThrow(new RuntimeException("Before delete error"));

        assertThatThrownBy(() -> service.delete(1L))
                .isExactlyInstanceOf(DeleteException.class)
                .hasMessage("Before delete error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(beforeDeleteHookA).apply(1L);
        verifyZeroInteractions(beforeDeleteHookB);
        verifyZeroInteractions(afterDeleteHookA);
        verifyZeroInteractions(afterDeleteHookB);
        verify(repository).existsById(1L);
        verifyZeroInteractions(repository);
    }

    @Test
    public void whenAfterCreateHookThrowsExceptionThenThrowCreateException() {
        addHooks();

        when(repository.existsById(1L)).thenReturn(true);
        when(beforeDeleteHookA.apply(1L)).thenReturn(1L);
        when(beforeDeleteHookB.apply(1L)).thenReturn(1L);
        when(afterDeleteHookA.apply(1L)).thenThrow(new RuntimeException("After delete error"));

        assertThatThrownBy(() -> service.delete(1L))
                .isExactlyInstanceOf(DeleteException.class)
                .hasMessage("After delete error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
        verify(beforeDeleteHookA).apply(1L);
        verify(beforeDeleteHookB).apply(1L);
        verify(afterDeleteHookA).apply(1L);
        verifyZeroInteractions(afterDeleteHookB);
    }

    private Example buildEntityWithId(Long id) {
        Example entity = new Example();
        entity.setId(id);
        entity.setStringProperty("string");
        return entity;
    }

    private void addHooks() {
        service.addBeforeDeleteHook(beforeDeleteHookA);
        service.addBeforeDeleteHook(beforeDeleteHookB);
        service.addAfterDeleteHook(afterDeleteHookA);
        service.addAfterDeleteHook(afterDeleteHookB);

        reset(beforeDeleteHookA, beforeDeleteHookB, afterDeleteHookA, afterDeleteHookB);
    }
}
