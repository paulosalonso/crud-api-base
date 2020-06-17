package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class LocalTimeTypeMapper implements RepresentationTypeMapper<LocalTime> {

    @Override
    public String map() {
        return "string (pattern: HH:mm:ss)";
    }
}
