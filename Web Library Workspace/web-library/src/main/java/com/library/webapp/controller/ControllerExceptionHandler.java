package com.library.webapp.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.library.webapp.exceptions.DuplicateException;
import com.library.webapp.exceptions.NotFoundException;

@ControllerAdvice("com.library.webapp.controller")
public class ControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFoundException(NotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorPage");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }
    
    @ExceptionHandler(DuplicateException.class)
	 public ModelAndView  handleDuplicateException(DuplicateException ex) {
    	ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorPage");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
	 }
}

