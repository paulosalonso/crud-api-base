package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
public class DateTypeMapper implements RepresentationTypeMapper<Date> {

    @Override
    public String map(Class<Date> type) {
        return "string (pattern: yyyy-MM-dd)";
    }
}
