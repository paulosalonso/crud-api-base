package com.alon.spring.crud.service.exception;

public class UpdateException extends RuntimeException {

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
