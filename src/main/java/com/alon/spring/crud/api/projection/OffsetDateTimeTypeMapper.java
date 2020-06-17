package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Component
public class OffsetDateTimeTypeMapper implements RepresentationTypeMapper<OffsetDateTime> {

    @Override
    public String map(Class<OffsetDateTime> type) {
        return "string (pattern: yyyy-MM-ddTHH:mm:ssZ)";
    }
}
