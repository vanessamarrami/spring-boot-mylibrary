package com.library.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginWebController {
	
	@GetMapping
	public String getlogin(Model model) {
		return "login";
	}
	
}
