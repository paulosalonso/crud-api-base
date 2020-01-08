package com.alon.spring.crud.service.exception;

public class DeleteException extends RuntimeException {

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
