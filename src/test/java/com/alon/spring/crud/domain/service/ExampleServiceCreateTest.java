package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleCrudRepository;
import com.alon.spring.crud.domain.service.exception.CreateException;
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


public class ExampleServiceCreateTest {

    @InjectMocks
    private ExampleService service;

    @Mock
    private ExampleCrudRepository repository;
    
    @Mock
    private Function<Example, Example> beforeCreateHookA;

    @Mock
    private Function<Example, Example> beforeCreateHookB;

    @Mock
    private Function<Example, Example> afterCreateHookA;

    @Mock
    private Function<Example, Example> afterCreateHookB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service.clearHooks(BEFORE_CREATE, AFTER_CREATE);
    }

    @Test
    public void whenSimpleCreateThenReturnCreated() {
        Example toCreate = buildEntityWithId(null);
        Example created = buildEntityWithId(1L);

        when(repository.save(toCreate)).thenReturn(created);

        Example result = service.create(toCreate);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStringProperty()).isEqualTo("string");

        verify(repository).save(toCreate);
    }

    @Test
    public void whenRepositoryThrowsExceptionThenThrowCreateException() {
        Example toCreate = buildEntityWithId(null);

        when(repository.save(toCreate)).thenThrow(new RuntimeException("Error saving"));

        assertThatThrownBy(() -> service.create(toCreate))
                .isExactlyInstanceOf(CreateException.class)
                .hasMessage("Error saving")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(repository).save(toCreate);
    }

    @Test
    public void whenCreateWithHooksThenExecuteHooks() {
        addHooks();

        Example toCreate = buildEntityWithId(null);
        Example created = buildEntityWithId(1L);

        when(repository.save(toCreate)).thenReturn(created);
        when(beforeCreateHookA.apply(toCreate)).thenReturn(toCreate);
        when(beforeCreateHookB.apply(toCreate)).thenReturn(toCreate);
        when(afterCreateHookA.apply(created)).thenReturn(created);
        when(afterCreateHookB.apply(created)).thenReturn(created);

        service.create(toCreate);

        verify(repository).save(toCreate);

        InOrder inOrder = inOrder(beforeCreateHookA, beforeCreateHookB, afterCreateHookA, afterCreateHookB);
        inOrder.verify(beforeCreateHookA).apply(toCreate);
        inOrder.verify(beforeCreateHookB).apply(toCreate);
        inOrder.verify(afterCreateHookA).apply(created);
        inOrder.verify(afterCreateHookB).apply(created);
    }

    @Test
    public void whenBeforeCreateHookThrowsExceptionThenThrowReadException() {
        addHooks();

        Example toCreate = buildEntityWithId(null);

        when(beforeCreateHookA.apply(toCreate)).thenThrow(new RuntimeException("Before create error"));

        assertThatThrownBy(() -> service.create(toCreate))
                .isExactlyInstanceOf(CreateException.class)
                .hasMessage("Before create error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(beforeCreateHookA).apply(toCreate);
        verifyZeroInteractions(beforeCreateHookB);
        verifyZeroInteractions(afterCreateHookA);
        verifyZeroInteractions(afterCreateHookB);
        verifyZeroInteractions(repository);
    }

    @Test
    public void whenAfterCreateHookThrowsExceptionThenThrowCreateException() {
        addHooks();

        Example toCreate = buildEntityWithId(null);
        Example created = buildEntityWithId(1L);

        when(repository.save(toCreate)).thenReturn(created);
        when(beforeCreateHookA.apply(toCreate)).thenReturn(toCreate);
        when(beforeCreateHookB.apply(toCreate)).thenReturn(toCreate);
        when(afterCreateHookA.apply(created)).thenThrow(new RuntimeException("After create error"));

        assertThatThrownBy(() -> service.create(toCreate))
                .isExactlyInstanceOf(CreateException.class)
                .hasMessage("After create error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);

        verify(repository).save(toCreate);
        verify(beforeCreateHookA).apply(toCreate);
        verify(beforeCreateHookB).apply(toCreate);
        verify(afterCreateHookA).apply(created);
        verifyZeroInteractions(afterCreateHookB);
    }

    private Example buildEntityWithId(Long id) {
        Example entity = new Example();
        entity.setId(id);
        entity.setStringProperty("string");
        return entity;
    }

    private void addHooks() {
        service.addBeforeCreateHook(beforeCreateHookA);
        service.addBeforeCreateHook(beforeCreateHookB);
        service.addAfterCreateHook(afterCreateHookA);
        service.addAfterCreateHook(afterCreateHookB);

        reset(beforeCreateHookA, beforeCreateHookB, afterCreateHookA, afterCreateHookB);
    }
}
