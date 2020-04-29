package com.alon.spring.crud.api.exceptionhandler;

import org.springframework.http.HttpStatus;

public enum ProblemType {

	NOT_FOUND("Not found", "/not-found"),
	INTEGRITY_VIOLATION("Integrity violation", "/integrity-violation"),
	UNRECOGNIZED_MESSAGE("Unrecognized message", "/unrecognized-message"),
	INVALID_PARAMETER("Invalid parameter", "/invalid-parameter"),
	INVALID_DATA("Invalid data", "/invalid-data"),
	INTERNAL_ERROR("Internal error", "/internal-error"),
	LOCKED("Locked resource", "/locked");
	
	private String title;
	private String uri;
	
	private ProblemType(String title, String path) {
		this.title = title;
		this.uri = "https://algafood.com.br".concat(path);
	}

	public String getTitle() {
		return title;
	}

	public String getUri() {
		return uri;
	}

	public static ProblemType getByStatusCode(HttpStatus httpStatus) {
		switch (httpStatus) {
			case NOT_FOUND: return NOT_FOUND;
			case LOCKED: return LOCKED;
			default: return INTERNAL_ERROR;
		}
	}
	
}
