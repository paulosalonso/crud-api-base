package com.alon.spring.crud.service;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.dto.EntityListProjection;
import com.alon.spring.crud.resource.dto.EntityProjection;
import com.alon.spring.crud.resource.dto.ListOutput;
import com.alon.spring.crud.resource.dto.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectionService {

    @Autowired
    private ApplicationContext applicationContext;

    public <I extends BaseEntity, O> O project(String projection, I input) {
        try {
            return (O) this.getProjection(projection).project(input);
        } catch (Exception e) {
            throw new ProjectionException(e);
        }
    }

    public <I extends BaseEntity, O> ListOutput<O> project(String projection, Page<I> input) {
        try {
            return (ListOutput<O>) this.getListProjection(projection).project(input);
        } catch (Exception e) {
            throw new ProjectionException(e);
        }
    }

    private Projection getProjection(String projectionName) {

        Projection projection = this.applicationContext.getBeansOfType(Projection.class).get(projectionName);

        return Optional.ofNullable(projection)
                       .orElse(this.applicationContext.getBean(EntityProjection.class));

    }

    private Projection getListProjection(String projectionName) {

        Projection projection = this.applicationContext.getBeansOfType(Projection.class).get(projectionName);

        return Optional.ofNullable(projection)
                       .orElse(this.applicationContext.getBean(EntityListProjection.class));

    }

}
