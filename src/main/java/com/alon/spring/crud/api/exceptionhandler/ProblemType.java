package com.alon.spring.crud.api.exceptionhandler;

public enum ProblemType {

	BUSINESS_ERROR("Business error", "/business-error"),
	NOT_FOUND("Not found", "/not-found"),
	INTEGRITY_VIOLATION("Integrity violation", "/integrity-violation"),
	UNRECOGNIZED_MESSAGE("Unrecognized message", "/unrecognized-message"),
	INVALID_PARAMETER("Invalid parameter", "/invalid-parameter"),
	INVALID_DATA("Invalid data", "/invalid-data"),
	INTERNAL_ERROR("Internal error", "/internal-error");
	
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
	
}
