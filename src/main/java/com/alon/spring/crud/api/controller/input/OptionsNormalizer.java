package com.alon.spring.crud.api.controller.input;

import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.crud.domain.service.exception.ProjectionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

@Component
public class OptionsNormalizer {

    private Properties properties;
    private ProjectionService projectionService;

    public OptionsNormalizer(Properties properties, ProjectionService projectionService) {
        this.properties = properties;
        this.projectionService = projectionService;
    }

    public boolean projectDefaultOnError(String projection, Supplier<String> defaultProjectionSupplier) {
        return properties.projection.useDefaultIfError
                && !projection.equals(defaultProjectionSupplier.get());
    }

    public void normalizeOptions(Options options,
            Supplier<String> defaultProjectionSupplier, Supplier<List<String>> allowedProjectionsSupplier) {

        normalizeProjection(options, defaultProjectionSupplier, allowedProjectionsSupplier);
        normalizeExpand(options);
    }

    private void normalizeProjection(Options options,
            Supplier<String> defaultProjectionSupplier, Supplier<List<String>> allowedProjectionsSupplier) {

        if (options.getProjection() == null) {
            options.setProjection(defaultProjectionSupplier.get());
        } else {
            List<String> allowedProjections = allowedProjectionsSupplier.get();

            if (!allowedProjections.isEmpty() && !allowedProjections.contains(options.getProjection()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projection not allowed");
        }
    }

    private void normalizeExpand(Options options) {
        if (!options.getProjection().equals(ProjectionService.NOP_PROJECTION)) {
            try {
                if (options.getExpand() != null)
                    options.getExpand().addAll(projectionService.getRequiredExpand(options.getProjection()));
                else
                    options.setExpand(projectionService.getRequiredExpand(options.getProjection()));
            } catch (ProjectionException e) {
                // NOP
            }
        }
    }
}
