package com.alon.spring.crud.domain.service.exception;

public class UpdateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public UpdateException() {
    }
    
    public UpdateException(String msg) {
        super(msg);
    }

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }
}
