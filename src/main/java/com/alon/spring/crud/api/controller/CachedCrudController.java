package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.cache.DeepETagResolver;
import com.alon.spring.crud.api.controller.cache.ETagPolicy;
import com.alon.spring.crud.api.controller.input.EntityInputMapper;
import com.alon.spring.crud.api.controller.input.InputMapper;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.exception.ReadException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.List;
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
		SERVICE_TYPE
>  {

	/**
	 * View ShallowEtagHeaderFilter.STREAMING_ATTRIBUTE
	 */
	private static final String STREAMING_ATTRIBUTE = ShallowEtagHeaderFilter.class.getName() + ".STREAMING";
	
	private final ETagPolicy eTagPolicy;
	private final DeepETagResolver deepETagResolver;

	public CachedCrudController(SERVICE_TYPE service, ETagPolicy eTagPolicy, DeepETagResolver deepETagResolver) {
		super(service, new EntityInputMapper(), new EntityInputMapper());
		this.eTagPolicy = eTagPolicy;
		this.deepETagResolver = deepETagResolver;
	}

	protected CachedCrudController(SERVICE_TYPE service, ETagPolicy eTagPolicy, DeepETagResolver deepETagResolver,
		 	InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper,
		 	InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper) {

		super(service, createInputMapper, updateInputMapper);
		this.eTagPolicy = eTagPolicy;
		this.deepETagResolver = deepETagResolver;
	}

    @Override
	@GetMapping("${com.alon.spring.crud.path.search:}")
	public ResponseEntity search(
			SEARCH_INPUT_TYPE filter,
			@RequestParam(required = false) List<String> order,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "100") Integer pageSize,
			@RequestParam(required = false) List<String> expand,
			@RequestParam(required = false) String projection,
			ServletWebRequest request
	) {
		ResponseEntity response;

		if (eTagPolicy.equals(ETagPolicy.DISABLED) || eTagPolicy.equals(ETagPolicy.SHALLOW)) {
			response = super.search(filter, order, page, pageSize, expand, projection, request);
		} else {
			String eTag = deepETagResolver.generateCollectionResourceETag(getManagedEntityType(), filter);

			if (request.checkNotModified(eTag))
				response = buildResponseEntity(HttpStatus.NOT_MODIFIED)
						.header(HttpHeaders.ETAG, eTag)
						.build();
			else
				response = super.search(filter, order, page, pageSize, expand, projection, request);
		}

		if (eTagPolicy.equals(ETagPolicy.SHALLOW))
			enableContentCaching(request.getRequest());

		return response;
	}

	@GetMapping("${com.alon.spring.crud.path.read:/{id}}")
	public ResponseEntity read(
			@PathVariable MANAGED_ENTITY_ID_TYPE id,
			@RequestParam(required = false) List<String> expand,
			@RequestParam(required = false) String projection,
			ServletWebRequest request
	) throws ReadException {
		ResponseEntity response;

		if (eTagPolicy.equals(ETagPolicy.DISABLED) || eTagPolicy.equals(ETagPolicy.SHALLOW)) {
			response = super.read(id, expand, projection, request);
		} else {
			String eTag = deepETagResolver.generateSingleResourceETag(getManagedEntityType(), id);

			if (request.checkNotModified(eTag))
				response = buildResponseEntity(HttpStatus.NOT_MODIFIED)
						.header(HttpHeaders.ETAG, eTag)
						.build();
			else
				response = super.read(id, expand, projection, request);
		}

		if (eTagPolicy.equals(ETagPolicy.SHALLOW))
			enableContentCaching(request.getRequest());

		return response;
	}

	@Override
	public BodyBuilder buildResponseEntity(HttpStatus status) {
		return ResponseEntity
				.status(status)
				.cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS));
		// TODO Configurar maxAge via properties
	}

	/**
	 * View ShallowEtagHeaderFilter.disableContentCaching
	 * and ShallowEtagHeaderFilter.isContentCachingDisabled
	 */
	private static void enableContentCaching(ServletRequest request) {
		request.setAttribute(STREAMING_ATTRIBUTE, null);
	}

	private <ID> boolean callSuper(ID id, ServletWebRequest request) {
		if (eTagPolicy.equals(ETagPolicy.DISABLED) || eTagPolicy.equals(ETagPolicy.SHALLOW))
			return true;

		String eTag = deepETagResolver.generateSingleResourceETag(getManagedEntityType(), id);
		return !request.checkNotModified(eTag);
	}

	private boolean callSuper(SEARCH_INPUT_TYPE filter, ServletWebRequest request) {
		if (eTagPolicy.equals(ETagPolicy.DISABLED) || eTagPolicy.equals(ETagPolicy.SHALLOW))
			return true;

		String eTag = deepETagResolver.generateCollectionResourceETag(getManagedEntityType(), filter);
		return !request.checkNotModified(eTag);
	}
	
}
