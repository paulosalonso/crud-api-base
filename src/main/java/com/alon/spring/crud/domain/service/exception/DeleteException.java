package com.alon.spring.crud.domain.service.exception;

public class DeleteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public DeleteException() {
    }
    
    public DeleteException(String msg) {
        super(msg);
    }

    public DeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteException(Throwable cause) {
        super(cause);
    }
}
