package com.alon.spring.crud.api.exceptionhandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.alon.spring.crud.api.exception.BeanValidationException;
import com.alon.spring.crud.domain.service.exception.DataIntegrityException;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	private MessageSource messageSource;
	
	public ApiExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		String detail = "Ocorreu um erro inesperado no sistema. "
				+ "Se o problema persistir, entre em contato com o administrador.";
		
		Problem problem = createProblemBuilder(status, ProblemType.INTERNAL_ERROR, detail)
				.build();
		
		ex.printStackTrace();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		
		if (rootCause instanceof InvalidFormatException) {
			return handleInvalidFormatException((InvalidFormatException) rootCause, headers, status, request);
		} else if (rootCause instanceof PropertyBindingException) {
			return handlePropertyBindingException((PropertyBindingException) rootCause, headers, status, request);
		}
		
		String detail = "O corpo da requisição é inválido. Verifique erro de sintaxe.";
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String detail = "O valor '%' não é válido para a propriedade '%s'. "
				+ "Informe um valor do tipo %s.";
		
		String field = joinPath(ex.getPath());
		
		detail = String.format(detail, ex.getValue(), field, ex.getTargetType().getSimpleName());
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@ExceptionHandler(PropertyBindingException.class)
	public ResponseEntity<Object> handlePropertyBindingException(PropertyBindingException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String field = joinPath(ex.getPath());
		String detail = String.format("A propriedade '%s' é inválida.", field);
		
		Problem problem = createProblemBuilder(status, ProblemType.UNRECOGNIZED_MESSAGE, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if (ex instanceof MethodArgumentTypeMismatchException) {
			return handleMethodArgumentTypeMismatchException(
					(MethodArgumentTypeMismatchException) ex, headers, status, request);
		}
	
		return super.handleTypeMismatch(ex, headers, status, request);
	}
	
	public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String detail = "O parâmetro de URL '%s' recebeu o valor '%s' é inválido. Informe um valor do tipo %s.";
		
		detail = String.format(detail, ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_PARAMETER, detail)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		
		return handleValidationInternal(ex, headers, status, request, ex.getBindingResult());
	}
	
	@Override
	public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		return handleValidationInternal(ex, headers, status, request, ex.getBindingResult());
	}

	private ResponseEntity<Object> handleValidationInternal(Exception ex, HttpHeaders headers,
			HttpStatus status, WebRequest request, BindingResult bindingResult	) {
		
		String detail = "Existe(m) campo(s) inválido(s).";
		
		List<Problem.Violation> problemFields = this.createProblemFields(bindingResult);
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_DATA, detail)
				.violations(problemFields)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		String detail = String.format("O recurso '%s' não existe.", ex.getRequestURL());
		
		Problem problem = createProblemBuilder(status, ProblemType.NOT_FOUND, detail)
				.build();
		
		return super.handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@ExceptionHandler(BeanValidationException.class)
	public ResponseEntity<Object> handleBeanValidation(BeanValidationException ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String detail = "Existe(m) campo(s) inválido(s).";
		
		List<Problem.Violation> problemFields = this.createProblemFields(ex.getBindingResult());
		
		Problem problem = createProblemBuilder(status, ProblemType.INVALID_DATA, detail)
				.violations(problemFields)
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<?> handleEntidadeNaoEncontradaException(
			NotFoundException ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		Problem problem = createProblemBuilder(status, ProblemType.NOT_FOUND, ex.getMessage())
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(DataIntegrityException.class)
	public ResponseEntity<?> handleEntidadeEmUsoException(
			DataIntegrityException ex, WebRequest request) {
		
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		Problem problem = createProblemBuilder(status, ProblemType.INTEGRITY_VIOLATION, ex.getMessage())
				.build();
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), 
				status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
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
