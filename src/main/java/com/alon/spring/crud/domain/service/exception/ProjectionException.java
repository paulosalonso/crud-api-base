package com.alon.spring.crud.domain.service.exception;

public class ProjectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ProjectionException() {
    }

    public ProjectionException(String message) {
        super(message);
    }

    public ProjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectionException(Throwable cause) {
        super(cause);
    }

    public ProjectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
