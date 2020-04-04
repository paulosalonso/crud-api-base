package com.alon.spring.crud.core.properties;

public class CacheControlInvalidConfigurationException extends RuntimeException {
    public CacheControlInvalidConfigurationException() {
    }

    public CacheControlInvalidConfigurationException(String message) {
        super(message);
    }

    public CacheControlInvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheControlInvalidConfigurationException(Throwable cause) {
        super(cause);
    }

    public CacheControlInvalidConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
