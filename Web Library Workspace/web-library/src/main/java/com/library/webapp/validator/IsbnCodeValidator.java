package com.library.webapp.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnCodeValidator implements ConstraintValidator<IsbnCodeConstraint, String>{

	@Override
	public boolean isValid(String isbnValue, ConstraintValidatorContext context) {
		return isbnValue != null && isbnValue.matches("^(978|979)-[0-9]{1,5}-[0-9]{2,6}-[0-9]+-[0-9]{1}$")
				&& (isbnValue.length() > 13) && (isbnValue.length() < 18);
	}

}
