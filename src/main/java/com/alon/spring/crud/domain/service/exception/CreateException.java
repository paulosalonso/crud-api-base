package com.alon.spring.crud.domain.service.exception;

public class CreateException extends CrudException {

    private static final long serialVersionUID = 1L;

	public CreateException() {
    }
    
    public CreateException(String msg) {
        super(msg);
    }

    public CreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateException(Throwable cause) {
        super(cause);
    }
}
