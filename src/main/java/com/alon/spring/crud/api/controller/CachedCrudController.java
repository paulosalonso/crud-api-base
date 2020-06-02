package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.cache.DeepETagResolver;
import com.alon.spring.crud.api.controller.cache.ETagPolicy;
import com.alon.spring.crud.api.controller.input.mapper.InputMapper;
import com.alon.spring.crud.api.controller.input.Options;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.crud.core.properties.Properties.CacheControlProperties;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.exception.ReadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public abstract class CachedCrudController<
		MANAGED_ENTITY_ID_TYPE extends Serializable,
		MANAGED_ENTITY_TYPE extends BaseEntity<MANAGED_ENTITY_ID_TYPE>,
		CREATE_INPUT_TYPE,
		UPDATE_INPUT_TYPE,
		SEARCH_INPUT_TYPE extends SearchInput,
		SERVICE_TYPE extends CrudService<MANAGED_ENTITY_ID_TYPE, MANAGED_ENTITY_TYPE, ?>
> extends CrudController<
		MANAGED_ENTITY_ID_TYPE,
		MANAGED_ENTITY_TYPE,
		CREATE_INPUT_TYPE,
		UPDATE_INPUT_TYPE,
		SEARCH_INPUT_TYPE,
		SERVICE_TYPE>  {

	private final ETagPolicy eTagPolicy;

	@Autowired
	private DeepETagResolver deepETagResolver;

	@Autowired
	private Properties properties;

	/**
	 * Creates a CachedCrudController with disabled ETag feature
	 */
	protected CachedCrudController(SERVICE_TYPE service) {
		super(service, false);
		this.eTagPolicy = ETagPolicy.DISABLED;
	}

	/**
	 * Creates a CachedCrudController with the provided ETag policy.
	 * The DeepETagResolver instance is automatically injected and, if
	 * there are not exists custom implementations of single and/or collection
	 * DeepETagGenerator (annotated with @Primary), the default implementations
	 * will be used to resolve ETags. These implementations are based on the
	 * updateTimestamp property of the entity (provided by BaseEntity).
	 */
	protected CachedCrudController(SERVICE_TYPE service, ETagPolicy eTagPolicy) {
		super(service, disableContentCachingInCrudController(eTagPolicy));
		this.eTagPolicy = eTagPolicy;
	}

	protected CachedCrudController(SERVICE_TYPE service, ETagPolicy eTagPolicy, DeepETagResolver deepETagResolver) {
		super(service, disableContentCachingInCrudController(eTagPolicy));
		this.eTagPolicy = eTagPolicy;
		this.deepETagResolver = deepETagResolver;
	}

	protected CachedCrudController(SERVICE_TYPE service, ETagPolicy eTagPolicy, DeepETagResolver deepETagResolver,
		 	InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper,
		 	InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper) {

		super(service, createInputMapper, updateInputMapper, false);
		this.eTagPolicy = eTagPolicy;
		this.deepETagResolver = deepETagResolver;
	}

	private static boolean disableContentCachingInCrudController(ETagPolicy eTagPolicy) {
		return !eTagPolicy.equals(ETagPolicy.SHALLOW);
	}

	@Override
	@GetMapping("${com.alon.spring.crud.path.search:}")
	public ResponseEntity search(
			SEARCH_INPUT_TYPE search,
			Pageable pageable,
			Options options,
			ServletWebRequest request
	) {
		ResponseEntity response;

		if (eTagPolicy.equals(ETagPolicy.DISABLED) || eTagPolicy.equals(ETagPolicy.SHALLOW)) {
			response = super.search(search, pageable, options, request);
		} else {
			String eTag = deepETagResolver.generateCollectionResourceETag(managedEntityClass, search);

			if (request.checkNotModified(eTag))
				response = buildHttpGETResponseEntity(HttpStatus.NOT_MODIFIED)
						.header(HttpHeaders.ETAG, eTag)
						.build();
			else
				response = super.search(search, pageable, options, request);
		}

		return response;
	}

	@GetMapping("${com.alon.spring.crud.path.read:/{id}}")
	public ResponseEntity read(
			@PathVariable MANAGED_ENTITY_ID_TYPE id,
			Options options,
			ServletWebRequest request
	) throws ReadException {
		ResponseEntity response;

		if (eTagPolicy.equals(ETagPolicy.DISABLED) || eTagPolicy.equals(ETagPolicy.SHALLOW)) {
			response = super.read(id, options, request);
		} else {
			String eTag = deepETagResolver.generateSingleResourceETag(managedEntityClass, id);

			if (request.checkNotModified(eTag))
				response = buildHttpGETResponseEntity(HttpStatus.NOT_MODIFIED)
						.header(HttpHeaders.ETAG, eTag)
						.build();
			else
				response = super.read(id, options, request);
		}

		return response;
	}

	@Override
	public BodyBuilder buildHttpGETResponseEntity(HttpStatus status) {
		return ResponseEntity
				.status(status)
				.cacheControl(buildCacheControl());
	}

	private CacheControl buildCacheControl() {
		CacheControlProperties cacheControlProperties = properties.cacheControl;
		CacheControl cacheControl;

		if (cacheControlProperties.noCache)
			cacheControl = CacheControl.noCache();
		else if (cacheControlProperties.noStore)
			cacheControl = CacheControl.noStore();
		else
			cacheControl = CacheControl.maxAge(cacheControlProperties.maxAge, TimeUnit.SECONDS);

		if (cacheControlProperties.cachePublic)
			cacheControl.cachePublic();
		else if (cacheControlProperties.cachePrivate)
			cacheControl.cachePrivate();

		return cacheControl;
	}
	
}
