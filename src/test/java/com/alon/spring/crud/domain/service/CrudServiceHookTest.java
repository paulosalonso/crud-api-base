package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.EntityTest;
import com.alon.spring.crud.domain.repository.EntityTestRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.function.Function;

import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CrudServiceHookTest {

    @InjectMocks
    private CrudTestService service;

    @Mock
    private EntityTestRepository repository;

    @Mock
    private Function<SearchCriteria, SearchCriteria> beforeSearch;

    @Mock
    private Function<Page<EntityTest>, Page<EntityTest>> afterSearch;

    @Mock
    private Function<Long, Long> beforeRead;

    @Mock
    private Function<EntityTest, EntityTest> afterRead;

    @Mock
    private Function<EntityTest, EntityTest> beforeCreate;

    @Mock
    private Function<EntityTest, EntityTest> afterCreate;

    @Mock
    private Function<EntityTest, EntityTest> beforeUpdate;

    @Mock
    private Function<EntityTest, EntityTest> afterUpdate;

    @Mock
    private Function<Long, Long> beforeDelete;

    @Mock
    private Function<Long, Long> afterDelete;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void whenSearchAndClearHooksThenDoNotExecuteHooks() {
        service.addBeforeSearchHook(beforeSearch);
        service.addAfterSearchHook(afterSearch);
        service.clearHooks(BEFORE_SEARCH, AFTER_SEARCH);

        service.search(SearchCriteria.of().build());

        verifyZeroInteractions(beforeSearch);
        verifyZeroInteractions(afterSearch);
    }

    @Test
    public void whenReadAndClearHooksThenDoNotExecuteHooks() {
        service.addBeforeReadHook(beforeRead);
        service.addAfterReadHook(afterRead);
        service.clearHooks(BEFORE_READ, AFTER_READ);

        when(repository.findById(1L)).thenReturn(Optional.of(new EntityTest()));

        service.read(1L);

        verify(repository).findById(1L);
        verifyZeroInteractions(beforeRead);
        verifyZeroInteractions(afterRead);
    }

    @Test
    public void whenCreateAndClearHooksThenDoNotExecuteHooks() {
        service.addBeforeCreateHook(beforeCreate);
        service.addAfterCreateHook(afterCreate);
        service.clearHooks(BEFORE_CREATE, AFTER_CREATE);

        when(repository.save(any())).thenReturn(new EntityTest());

        service.create(new EntityTest());

        verify(repository).save(any());
        verifyZeroInteractions(beforeCreate);
        verifyZeroInteractions(afterCreate);
    }

    @Test
    public void whenUpdateAndClearHooksThenDoNotExecuteHooks() {
        service.addBeforeUpdateHook(beforeUpdate);
        service.addAfterUpdateHook(afterUpdate);
        service.clearHooks(BEFORE_UPDATE, AFTER_UPDATE);

        when(repository.existsById(any())).thenReturn(true);
        when(repository.save(any())).thenReturn(new EntityTest());

        service.update(new EntityTest());

        verify(repository).save(any());
        verifyZeroInteractions(beforeCreate);
        verifyZeroInteractions(afterCreate);
    }

    @Test
    public void whenDeleteAndClearHooksThenDoNotExecuteHooks() {
        service.addBeforeDeleteHook(beforeDelete);
        service.addAfterDeleteHook(afterDelete);
        service.clearHooks(BEFORE_DELETE, AFTER_DELETE);

        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(any())).thenReturn(new EntityTest());

        service.delete(1L);

        verify(repository).deleteById(1L);
        verifyZeroInteractions(beforeDelete);
        verifyZeroInteractions(afterDelete);
    }

}
