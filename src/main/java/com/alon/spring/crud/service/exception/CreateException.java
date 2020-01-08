package com.alon.spring.crud.service.exception;

public class CreateException extends RuntimeException {

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
