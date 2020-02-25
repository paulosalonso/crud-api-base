package com.alon.spring.crud.api.exception;

import org.springframework.validation.BindingResult;

public class BeanValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private BindingResult bindingResult;

	public BeanValidationException(BindingResult bindingResult) {
		this.bindingResult = bindingResult;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}

}
