package com.library.webapp.exceptions;
 
public class DuplicateException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public DuplicateException(String message)
	{
		super(message);
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
