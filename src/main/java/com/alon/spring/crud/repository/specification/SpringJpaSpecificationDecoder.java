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
import com.alon.querydecoder.MatchType;
import com.alon.querydecoder.QueryDecoder;
import com.alon.spring.crud.repository.specification.converter.ConverterResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    
    public SpringJpaSpecificationDecoder(String query) {
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
        try {
            return this.decode(root, criteriaBuilder);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Predicate decode(Root<T> root, CriteriaBuilder criteriaBuilder) throws Throwable {
        if (this.decoder instanceof Group)
            return this.decode((Group) this.decoder, root, criteriaBuilder);
        
        return this.decode((Expression) this.decoder, root, criteriaBuilder);
    }
    
    private Predicate decode(Decoder decoder, Root<T> root, CriteriaBuilder criteriaBuilder) throws Throwable {
        if (decoder instanceof Group)
            return this.decode((Group) decoder, root, criteriaBuilder);
        
        return this.decode((Expression) decoder, root, criteriaBuilder);
    }
    
    private Predicate decode(Group group, Root<T> root, CriteriaBuilder criteriaBuilder) throws Throwable {
        Predicate predicate = this.decode(group.getDecoder(), root, criteriaBuilder);
        return this.decodeNext(predicate, group, root, criteriaBuilder);
    }
    
    private Predicate decode(Expression expression, Root<T> root, CriteriaBuilder criteriaBuilder) throws Throwable {
        Predicate predicate;
        Path path = this.getPath(root, new ArrayList<>(Arrays.asList(expression.getField().split("\\."))));
        
        Object value, valueBT = null;
        
        if (expression.getMatchType().equals(MatchType.BT)) {
            String[] values = expression.getValue().split("-");
            value = this.convert(path, values[0]);
            valueBT = this.convert(path, values[1]);
        } else
            value = this.getValue(path, expression);
        
        switch (expression.getMatchType()) {
            case BT : predicate = criteriaBuilder.between(path, (Comparable) value, (Comparable) valueBT); break;
            case CT : predicate = criteriaBuilder.like(path, String.format("%%%s%%", value)); break;
            case EQ : predicate = this.resolveEqualOrNullOrNotNull(criteriaBuilder, path, value); break;
            case GT : predicate = criteriaBuilder.greaterThan(path, (Comparable) value); break;
            case GTE: predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) value); break;
            case LT : predicate = criteriaBuilder.lessThan(path, (Comparable) value); break;
            case LTE: predicate = criteriaBuilder.lessThanOrEqualTo(path, (Comparable) value); break;
            case IN : predicate = path.in(value); break;
            default : predicate = criteriaBuilder.equal(path, value);
        }
        
        return this.decodeNext(predicate, expression, root, criteriaBuilder);
    }
    
    private Predicate resolveEqualOrNullOrNotNull(CriteriaBuilder criteriaBuilder, Path path, Object value) {
        if (value.toString().equalsIgnoreCase("NULL"))
            return criteriaBuilder.isNull(path);
        else if (value.toString().equalsIgnoreCase("NOT NULL"))
            return criteriaBuilder.isNotNull(path);
        
        return criteriaBuilder.equal(path, value);
    }
    
    private Predicate decodeNext(Predicate predicate, Decoder decoder, Root<T> root, CriteriaBuilder criteriaBuilder) throws Throwable {
        if (decoder.getNext() == null)
            return predicate;
        
        Predicate nextPredicate = this.decode(decoder.getNext(), root, criteriaBuilder);

        if (decoder.getLogicalOperator().equals(LogicalOperator.AND))
            return criteriaBuilder.and(predicate, nextPredicate);

        return criteriaBuilder.or(predicate, nextPredicate);
    }
    
    private Path getPath(Path parent, List<String> props) {
        Path p = parent.get(props.remove(0));

        if (!props.isEmpty())
            return getPath(p, props);

        return p;
    }
    
    private Object convert(Path path, String value) throws Throwable {
        return ConverterResolver.resolve(path.getJavaType()).convert(value);
    }
    
    private Object getValue(Path path, Expression expression) throws Throwable {
        if (isNullOrNotNull(expression.getValue()))
            return expression.getValue();
        
        Object result;
        String[] values;
        
        switch (expression.getMatchType()) {
            case IN:
                values = expression.getValue().split((","));                
                result = new ArrayList<>();
                
                for (String v : values)
                    ((Collection) result).add(this.convert(path, v));
                
                break;
            
            default:
                result = this.convert(path, expression.getValue());
                break;
        }
        
        return result;
    }
    
    private boolean isNullOrNotNull(Object value) {
        return Arrays.asList(new String[]{ "NULL", "NOT NULL"})
                     .contains(value.toString().toUpperCase());
    }
    
}
