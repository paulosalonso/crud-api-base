package com.alon.spring.crud.api.controller.input;

import javax.validation.constraints.NotBlank;

import org.springframework.data.jpa.domain.Specification;

import com.alon.spring.specification.ExpressionSpecification;

public class ExpressionSearchInput implements SearchInput {

	@NotBlank
	private String expression;
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public Specification toSpecification() {
		return ExpressionSpecification.of(this.expression);
	}

}
