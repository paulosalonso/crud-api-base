package com.alon.spring.crud.domain.service.exception;

public class CrudException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CrudException() {
		super();
	}

	public CrudException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CrudException(String message, Throwable cause) {
		super(message, cause);
	}

	public CrudException(String message) {
		super(message);
	}

	public CrudException(Throwable cause) {
		super(cause);
	}

}
