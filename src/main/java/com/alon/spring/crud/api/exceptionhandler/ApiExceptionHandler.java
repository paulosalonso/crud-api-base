package com.alon.spring.crud.api.exceptionhandler;

import com.alon.spring.crud.api.exception.BeanValidationException;
import com.alon.spring.crud.api.exceptionhandler.Problem.Violation;
import com.alon.spring.crud.domain.service.exception.CrudException;
import com.alon.spring.crud.domain.service.exception.DataIntegrityException;
import com.alon.spring.crud.domain.service.exception.ProjectionException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	private MessageSource messageSource;
	
	public ApiExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity handleUncaught(Exception ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		String detail = "An internal server problem has occurred. If the problem persists, contact your administrator.";
		
		Problem problem = createProblemBuilder(status, ProblemType.INTERNAL_ERROR, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(CrudException.class)
	public ResponseEntity handleCrudException(CrudException ex, WebRequest request) {
		
		Throwable cause = ExceptionUtils.getRootCause(ex);
		
		if (cause instanceof PropertyReferenceException)
			return handlePropertyReferenceException((PropertyReferenceException) cause, new HttpHeaders(), request);
		
		return handleUncaught(ex, request);
		
	}

	@ExceptionHandler(ProjectionException.class)
	public ResponseEntity handleProjectionException(ProjectionException ex, WebRequest request) {
		HttpStatus status;
		String detail;
		ProblemType type;

		if (request.getParameter("projection") != null) {
			status = HttpStatus.BAD_REQUEST;
			detail = ex.getMessage();
			type = ProblemType.INVALID_PARAMETER;
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			detail = "An error occurred when projecting the response. If the problem persists, contact your administrator.";
			type = ProblemType.INTERNAL_ERROR;
		}

		Problem problem = createProblemBuilder(status, type, detail).build();

		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}

	
	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity handlePropertyReferenceException(PropertyReferenceException ex, HttpHeaders headers, WebRequest request) {
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		String detail = String.format("Invalid property name %s", ex.getPropertyName());
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
		
	}

	@Override
	protected ResponseEntity handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		
		if (rootCause instanceof InvalidFormatException)
			return handleInvalidFormatException((InvalidFormatException) rootCause, headers, status, request);
		else if (rootCause instanceof PropertyBindingException)
			return handlePropertyBindingException((PropertyBindingException) rootCause, headers, status, request);
		
		String detail = "Invalid request body.";
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity handleInvalidFormatException(InvalidFormatException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String detail = "'%' is not a valid value for the '%s' property. The required type is %s.";
		
		String field = joinPath(ex.getPath());
		
		detail = String.format(detail, ex.getValue(), field, ex.getTargetType().getSimpleName());
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@ExceptionHandler(PropertyBindingException.class)
	public ResponseEntity handlePropertyBindingException(PropertyBindingException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String field = joinPath(ex.getPath());
		String detail = String.format("Invalid property '%s'.", field);
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if (ex instanceof MethodArgumentTypeMismatchException) {
			return handleMethodArgumentTypeMismatchException(
					(MethodArgumentTypeMismatchException) ex, headers, status, request);
		}
	
		return super.handleTypeMismatch(ex, headers, status, request);
	}
	
	public ResponseEntity handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String detail = "'%s' is an invalid value for the '%s' URL parameter. Required type is '%s'.";
		
		detail = String.format(detail, ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_PARAMETER, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		
		return handleValidationInternal(ex, headers, status, request, ex.getBindingResult());
	}
	
	@Override
	public ResponseEntity handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		return handleValidationInternal(ex, headers, status, request, ex.getBindingResult());
	}

	private ResponseEntity handleValidationInternal(Exception ex, HttpHeaders headers,
			HttpStatus status, WebRequest request, BindingResult bindingResult) {
		
		String detail = "Invalid field(s).";
		
		List<Problem.Violation> problemFields = this.createProblemFields(bindingResult);
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_DATA, detail)
				.violations(problemFields)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@Override
	protected ResponseEntity handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		String detail = String.format("Resource not found: '%s'", ex.getRequestURL());
		
		Problem problem = createProblemBuilder(status, ProblemType.NOT_FOUND, detail)
				.build();
		
		return super.handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@ExceptionHandler(BeanValidationException.class)
	public ResponseEntity handleBeanValidation(BeanValidationException ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String detail = "Invalid fields present.";
		
		List<Problem.Violation> problemFields = this.createProblemFields(ex.getBindingResult());
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_DATA, detail)
				.violations(problemFields)
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(DataIntegrityException.class)
	public ResponseEntity handleDataIntegrityException(DataIntegrityException ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		Problem problem = createProblemBuilder(status, ProblemType.INTEGRITY_VIOLATION, ex.getMessage())
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), 
				status, request);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		String detail = "There is a problem with the data sent.";
		
		Violation violation = Violation.of()
				.context("Original error message")
				.message(ex.getMessage())
				.build();
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_DATA, detail)
				.violations(List.of(violation))
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
		HttpStatus status = ex.getStatus();
		String detail = ex.getReason();

		Problem problem = createProblemBuilder(status,
				ProblemType.getByStatusCode(status), detail).build();

		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}
	
	@Override
	protected ResponseEntity handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if (body == null) {
			body = Problem.of()
					.status(status.value())
					.title(status.getReasonPhrase())
					.build();
		} else if (body instanceof String) {
			body = Problem.of()
					.status(status.value())
					.title((String) body)
					.build();
		}
		
		return super.handleExceptionInternal(ex, body, headers, status, request);
	}
	
	private List<Problem.Violation> createProblemFields(BindingResult bindingResult) {
		return bindingResult.getAllErrors().stream()
				.map(error -> {
					
					String message = messageSource.getMessage(error, LocaleContextHolder.getLocale());
					
					String name = error.getObjectName();
					
					if (error instanceof FieldError)
						name = ((FieldError) error).getField();
					
					return Problem.Violation.of()
							.context(name)
							.message(message)
							.build();
				})
				.collect(Collectors.toList());
	}
	
	private Problem.ProblemBuilder createProblemBuilder(HttpStatus status, ProblemType type, String detail) {
		
		return Problem.of()
				.timestamp(OffsetDateTime.now())
				.status(status.value())
				.type(type.getUri())
				.title(type.getTitle())
				.detail(detail);
		
	}
	
	private String joinPath(List<Reference> path) {
		return path.stream()
				.map(Reference::getFieldName)
				.collect(Collectors.joining("."));
	}
	
}
