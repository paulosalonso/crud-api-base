package com.alon.spring.crud.core.validation;

import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.core.properties.Properties;
import org.springframework.context.ApplicationContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.alon.spring.crud.core.context.ApplicationContextProvider.getApplicationContext;

public class ValidaProjectionValidator implements ConstraintValidator<ValidProjection, String> {

    private ProjectionService projectionService;
    private Properties properties;

    @Override
    public void initialize(ValidProjection constraintAnnotation) {
        ApplicationContext context = getApplicationContext();
        projectionService = (ProjectionService) context.getBean("projectionService");
        properties = (Properties) context.getBean("crudProperties");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean valid = projectionService.projectionExists(value);

        if (!valid) {
            String message = "projection %s not found";
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format(message, value))
                    .addConstraintViolation();
        }

        return valid;
    }
}
