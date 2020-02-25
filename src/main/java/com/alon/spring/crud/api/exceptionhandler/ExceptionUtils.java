package com.alon.spring.crud.api.exceptionhandler;

public class ExceptionUtils {

	public static Throwable getRootCause(Throwable throwable) {
		
		if (throwable.getCause() != null)
			return getRootCause(throwable.getCause());
		
		return throwable;
		
	}
	
}
