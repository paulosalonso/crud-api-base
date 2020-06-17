package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class LocalDateTimeTypeMapper implements RepresentationTypeMapper<LocalDateTime> {

    @Override
    public String map() {
        return "string (pattern: yyyy-MM-ddTHH:mm:ss)";
    }
}
