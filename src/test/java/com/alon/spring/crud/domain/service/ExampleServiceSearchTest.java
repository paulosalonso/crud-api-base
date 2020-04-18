package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.api.controller.input.ExampleSearchInput;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleRepository;
import com.alon.spring.crud.domain.service.exception.ReadException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.function.Function;

import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.AFTER_SEARCH;
import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.BEFORE_SEARCH;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ExampleServiceSearchTest {

    @InjectMocks
    private ExampleService service;

    @Mock
    private ExampleRepository repository;

    @Mock
    private Function<SearchCriteria, SearchCriteria> beforeSearchHookA;

    @Mock
    private Function<SearchCriteria, SearchCriteria> beforeSearchHookB;

    @Mock
    private Function<Page<Example>, Page<Example>> afterSearchHookA;

    @Mock
    private Function<Page<Example>, Page<Example>> afterSearchHookB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service.clearHooks(BEFORE_SEARCH, AFTER_SEARCH);
    }

    @Test
    public void whenSimpleSearchThenFindAll() {
        Pageable pageable = buildPageable();

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .build();

        service.search(criteria);

        verify(repository).findAll(pageable);
    }

    @Test
    public void whenSearchWithFilterThenFindAll() {
        Pageable pageable = buildPageable();
        Specification specification = buildSearchInput().toSpecification();

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .filter(specification)
                .build();

        service.search(criteria);

        verify(repository).findAll(specification, pageable);
    }

    @Test
    public void whenSearchWithExpandThenFindAll() {
        Pageable pageable = buildPageable();

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .expand(List.of("propertyA", "propertyB"))
                .build();

        service.search(criteria);

        verify(repository).findAll(pageable, criteria.getExpand());
    }

    @Test
    public void whenSearchWithFilterAndExpandThenFindAll() {
        Pageable pageable = buildPageable();
        Specification specification = buildSearchInput().toSpecification();

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .filter(specification)
                .expand(List.of("propertyA", "propertyB"))
                .build();

        service.search(criteria);

        verify(repository).findAll(specification, pageable, criteria.getExpand());
    }

    @Test
    public void whenRepositoryThrowsExceptionThenThrowReadException() {
        Pageable pageable = buildPageable();

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .build();

        when(repository.findAll(pageable)).thenThrow(new RuntimeException("Repository error"));

        assertThatThrownBy(() -> service.search(criteria))
                .isExactlyInstanceOf(ReadException.class)
                .hasMessage("Error searching entities: Repository error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void whenSearchWithHooksThenExecuteHooks() {
        addHooks();

        Pageable pageable = buildPageable();
        Page page = Page.empty(pageable);

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .build();

        when(repository.findAll(pageable)).thenReturn(page);
        when(beforeSearchHookA.apply(criteria)).thenReturn(criteria);
        when(beforeSearchHookB.apply(criteria)).thenReturn(criteria);
        when(afterSearchHookA.apply(page)).thenReturn(page);
        when(afterSearchHookB.apply(page)).thenReturn(page);

        service.search(criteria);

        InOrder inOrder = inOrder(beforeSearchHookA, beforeSearchHookB, afterSearchHookA, afterSearchHookB);
        inOrder.verify(beforeSearchHookA).apply(criteria);
        inOrder.verify(beforeSearchHookB).apply(criteria);
        inOrder.verify(afterSearchHookA).apply(page);
        inOrder.verify(afterSearchHookB).apply(page);
    }

    @Test
    public void whenBeforeSearchHookThrowsExceptionThenThrowReadException() {
        addHooks();

        Pageable pageable = buildPageable();

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .build();

        when(beforeSearchHookA.apply(criteria)).thenThrow(new RuntimeException("Before search error"));

        assertThatThrownBy(() -> service.search(criteria))
                .isExactlyInstanceOf(ReadException.class)
                .hasMessage("Error searching entities: Before search error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void whenAfterSearchHookThrowsExceptionThenThrowReadException() {
        addHooks();

        Pageable pageable = buildPageable();
        Page page = Page.empty(pageable);

        SearchCriteria criteria = SearchCriteria.of()
                .pageable(pageable)
                .build();

        when(repository.findAll(pageable)).thenReturn(page);
        when(afterSearchHookA.apply(page)).thenThrow(new RuntimeException("After search error"));

        assertThatThrownBy(() -> service.search(criteria))
                .isExactlyInstanceOf(ReadException.class)
                .hasMessage("Error searching entities: After search error")
                .hasCauseExactlyInstanceOf(RuntimeException.class);
    }

    private SearchInput buildSearchInput() {
        ExampleSearchInput searchInput = new ExampleSearchInput();
        searchInput.setStringProperty("test");
        return searchInput;
    }

    private Pageable buildPageable() {
        return PageRequest.of(1, 100);
    }

    private void addHooks() {
        service.addBeforeSearchHook(beforeSearchHookA);
        service.addBeforeSearchHook(beforeSearchHookB);
        service.addAfterSearchHook(afterSearchHookA);
        service.addAfterSearchHook(afterSearchHookB);

        reset(beforeSearchHookA, beforeSearchHookB, afterSearchHookA, afterSearchHookB);
    }

}
