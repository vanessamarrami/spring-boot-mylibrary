package com.library.webapp.exceptions;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = -1742371691046236031L;
	
	private String message;
	
	public NotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
