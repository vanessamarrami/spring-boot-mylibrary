package com.library.webapp.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = IsbnCodeValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsbnCodeConstraint {

	String message() default "Invalid ISBN code.";
	Class<?>[] groups() default{};
	Class<? extends Payload>[] payload() default{};
	
}



