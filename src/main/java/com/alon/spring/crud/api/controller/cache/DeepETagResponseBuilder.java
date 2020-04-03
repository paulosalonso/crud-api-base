package com.alon.spring.crud.api.controller.cache;

import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.BaseEntity;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class DeepETagResponseBuilder {

    private final DeepETagResolver deepETagResolver;

    public DeepETagResponseBuilder(DeepETagResolver deepETagResolver) {
        this.deepETagResolver = deepETagResolver;
    }

    public Optional<ResponseEntity> buildCollectionResponse(
            Class<? extends BaseEntity<?>> entityType, SearchInput search, ServletWebRequest request) {

        ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        String eTag = deepETagResolver.generateCollectionResourceETag(entityType, search);

        if (request.checkNotModified(eTag))
            return Optional.of(ResponseEntity
                    .status(HttpStatus.NOT_MODIFIED)
                    .header(HttpHeaders.CACHE_CONTROL,
                            CacheControl.maxAge(10, TimeUnit.SECONDS).getHeaderValue())
                    .build());

        return Optional.empty();
    }

    public <ID> Optional<ResponseEntity> buildSingleResponse(
            Class<? extends BaseEntity<?>> entityType, ServletWebRequest request, ID id) {

        ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        String eTag = deepETagResolver.generateSingleResourceETag(entityType, id);

        if (request.checkNotModified(eTag))
            return Optional.of(ResponseEntity
                    .status(HttpStatus.NOT_MODIFIED)
                    .header(HttpHeaders.CACHE_CONTROL,
                            CacheControl.maxAge(10, TimeUnit.SECONDS).getHeaderValue())
                    .build());

        return Optional.empty();
    }

}
