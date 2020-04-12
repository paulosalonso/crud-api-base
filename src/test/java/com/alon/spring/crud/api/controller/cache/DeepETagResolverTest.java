package com.alon.spring.crud.api.controller.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.EntityTest;

public class DeepETagResolverTest {

    @InjectMocks
    private DeepETagResolver deepETagResolver;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SingleResourceDeepETagGenerator singleResourceDeepETagGenerator;

    @Mock
    private CollectionResourceDeepETagGenerator collectionResourceDeepETagGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void whenGetSingleResourceETagThenCallGenerator() {
        when(singleResourceDeepETagGenerator.generate(eq(EntityTest.class),
                eq(entityManager), any(SearchInput.class))).thenReturn("abc");

        String eTag = deepETagResolver.generateSingleResourceETag(EntityTest.class, 1L);

        assertThat(eTag).isEqualTo("abc");

        ArgumentCaptor<SearchInput> searchInputCaptor = ArgumentCaptor.forClass(SearchInput.class);

        verify(singleResourceDeepETagGenerator)
                .generate(eq(EntityTest.class), eq(entityManager), searchInputCaptor.capture());

        SearchInput searchInput = searchInputCaptor.getValue();

        assertThat(searchInput.toSpecification()).isNotNull();
    }

    @Test
    public void whenGetCollectionResourceETagThenCallGenerator() {
        when(collectionResourceDeepETagGenerator.generate(eq(EntityTest.class),
                eq(entityManager), any(SearchInput.class))).thenReturn("abc");

        SearchInput searchInput = new SearchInput() {
            @Override
            public Specification toSpecification() {
                return (root, query, builder) -> null;
            }
        };

        String eTag = deepETagResolver.generateCollectionResourceETag(EntityTest.class, searchInput);

        assertThat(eTag).isEqualTo("abc");

        verify(collectionResourceDeepETagGenerator)
                .generate(EntityTest.class, entityManager, searchInput);
    }

}
