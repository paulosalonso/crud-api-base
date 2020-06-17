package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class LocalDateTypeMapper implements RepresentationTypeMapper<LocalDate> {

    @Override
    public String map() {
        return "string (pattern: yyyy-MM-dd)";
    }
}
