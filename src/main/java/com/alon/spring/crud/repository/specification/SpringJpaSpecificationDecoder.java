/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alon.spring.crud.repository.specification;

import com.alon.querydecoder.Decoder;
import com.alon.querydecoder.Expression;
import com.alon.querydecoder.Group;
import com.alon.querydecoder.LogicalOperator;
import com.alon.querydecoder.QueryDecoder;
import com.alon.spring.crud.repository.specification.predicate.PredicateBuilder;
import com.alon.spring.crud.repository.specification.predicate.PredicateBuilderResolver;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author paulo
 * @param <T>
 */
public class SpringJpaSpecificationDecoder<T> extends QueryDecoder<Predicate> implements Specification<T> {
    
    private SpringJpaSpecificationDecoder(String query) {
        super(query, SpringJpaSpecificationDecoder::decode);
    }
    
    private static Predicate decode(Decoder group) {
        throw new UnsupportedOperationException(
                            "This operation is unsupported. " + 
                            "This decoder was implemented to work in conjunction with " + 
                            "org.springframework.data.jpa.repository.JpaSpecificationExecutor methods."
        );
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        return this.decodeDecoder(super.decoder, root, criteriaBuilder);
    }
    
    private Predicate decodeDecoder(Decoder decoder, Root<T> root, CriteriaBuilder criteriaBuilder) {
        
        if (decoder instanceof Group)
            return this.decodeGroup((Group) decoder, root, criteriaBuilder);
        
        return this.decodeExpression((Expression) decoder, root, criteriaBuilder);
        
    }
    
    private Predicate decodeGroup(Group group, Root<T> root, CriteriaBuilder criteriaBuilder) {
        
        Predicate predicate = this.decodeDecoder(group.getDecoder(), root, criteriaBuilder);
        
        return this.decodeNext(predicate, group, root, criteriaBuilder);
        
    }
    
    private Predicate decodeExpression(Expression expression, Root<T> root, CriteriaBuilder criteriaBuilder) {
        
        PredicateBuilder predicateBuilder = PredicateBuilderResolver.resolve(expression.getMatchType());
        
        Path path = this.getPath(root, expression.getField());
        
        Predicate predicate = predicateBuilder.buildPredicate(criteriaBuilder, path, expression.getValue());
        
        return this.decodeNext(predicate, expression, root, criteriaBuilder);
        
    }
    
    private Predicate decodeNext(Predicate predicate, Decoder decoder, Root<T> root, CriteriaBuilder criteriaBuilder) {
        
        if (decoder.getNext() == null)
            return predicate;
        
        Predicate nextPredicate = this.decodeDecoder(decoder.getNext(), root, criteriaBuilder);

        if (decoder.getLogicalOperator().equals(LogicalOperator.AND))
            return criteriaBuilder.and(predicate, nextPredicate);

        return criteriaBuilder.or(predicate, nextPredicate);
        
    }
    
    private Path getPath(Path parentPath, String properties) {
        
        List<String> propertiesList = this.splitPropertiesChain(properties);
        
        String property = propertiesList.remove(0);
        Path path = parentPath.get(property);

        if (!propertiesList.isEmpty())
            return getPath(path, this.joinPropertiesChain(propertiesList));

        return path;
        
    }
    
    private List<String> splitPropertiesChain(String properties) {
        
        return Stream.of(properties.split("\\."))
                     .map(value -> new String(value))
                     .collect(Collectors.toList());
        
    }
    
    private String joinPropertiesChain(List<String> properties) {
        
        return properties.stream()
                         .collect(Collectors.joining("."));
        
    }
    
    public static SpringJpaSpecificationDecoder of(String query) {
        return new SpringJpaSpecificationDecoder(query);
    }
    
}
