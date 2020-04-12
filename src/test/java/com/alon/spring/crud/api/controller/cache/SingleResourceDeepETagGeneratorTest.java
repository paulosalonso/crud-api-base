package com.alon.spring.crud.api.controller.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.EntityTest;

public class SingleResourceDeepETagGeneratorTest {

    private static final String NOT_MODIFIED_ETAG = String.valueOf("NOT MODIFIED".hashCode());

    private SingleResourceDeepETagGenerator eTagGenerator = new SingleResourceDeepETagGeneratorImpl();

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder builder;

    @Mock
    private CriteriaQuery criteriaQuery;

    @Mock
    private Root from;

    @Mock
    private Specification specification;

    @Mock
    private Predicate predicate;

    @Mock
    private TypedQuery query;

    private SearchInput searchInput = new SearchInput() {
        @Override
        public Specification toSpecification() {
            return specification;
        }
    };

    @Before
    public void setUp() {
        initMocks(this);

        when(entityManager.getCriteriaBuilder()).thenReturn(builder);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(query);
        when(builder.createQuery()).thenReturn(criteriaQuery);
        when(criteriaQuery.from(any(Class.class))).thenReturn(from);
    }

    @After
    public void tearDown() {
        verify(entityManager).getCriteriaBuilder();
        verify(entityManager).createQuery(criteriaQuery);
        verify(builder).createQuery();
        verify(criteriaQuery).from(any(Class.class));
    }

    @Test
    public void whenGenerateETagThenReturn() {
        Date lastUpdate = new Date();

        when(query.getSingleResult()).thenReturn(lastUpdate);
        when(specification.toPredicate(from, criteriaQuery, builder)).thenReturn(predicate);

        String eTag = eTagGenerator.generate(EntityTest.class, entityManager, searchInput);

        assertThat(eTag).isEqualTo(String.valueOf(lastUpdate.hashCode()));

        verify(query).getSingleResult();
        verify(specification).toPredicate(from, criteriaQuery, builder);
        verify(criteriaQuery).where(predicate);
    }

    @Test
    public void whenUpdateTimestampIsNullThenReturnNotModifiedETag() {
        when(query.getSingleResult()).thenThrow(NoResultException.class);

        when(specification.toPredicate(from, criteriaQuery, builder)).thenReturn(predicate);

        String eTag = eTagGenerator.generate(EntityTest.class, entityManager, searchInput);

        assertThat(eTag).isEqualTo(NOT_MODIFIED_ETAG);

        verify(query).getSingleResult();
        verify(specification).toPredicate(from, criteriaQuery, builder);
        verify(criteriaQuery).where(predicate);
    }

    @Test
    public void whenSpecificationReturnNullPredicateThenReturn() {
        Date lastUpdate = new Date();

        when(query.getSingleResult()).thenReturn(lastUpdate);
        when(specification.toPredicate(from, criteriaQuery, builder)).thenReturn(null);

        String eTag = eTagGenerator.generate(EntityTest.class, entityManager, searchInput);

        assertThat(eTag).isEqualTo(String.valueOf(lastUpdate.hashCode()));

        verify(query).getSingleResult();
        verify(specification).toPredicate(from, criteriaQuery, builder);
        verify(criteriaQuery, never()).where(any(Predicate.class));
    }

    @Test
    public void whenSpecificationIsNullThenReturn() {
        Date lastUpdate = new Date();

        when(query.getSingleResult()).thenReturn(lastUpdate);

        SearchInput input = new SearchInput() {
            @Override
            public Specification toSpecification() {
                return null;
            }
        };

        String eTag = eTagGenerator.generate(EntityTest.class, entityManager, input);

        assertThat(eTag).isEqualTo(String.valueOf(lastUpdate.hashCode()));

        verify(query).getSingleResult();
        verify(criteriaQuery, never()).where(any(Predicate.class));
    }

}
