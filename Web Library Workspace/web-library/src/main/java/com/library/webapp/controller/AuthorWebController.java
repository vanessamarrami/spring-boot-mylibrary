package com.library.webapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.service.LibraryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping(value = "/library/author")
public class AuthorWebController {

	@Autowired
	LibraryService service;
	
	@GetMapping
	public String listAllAuthors(Model model) {
		List<AuthorDto> allAuthors = service.selAllAuthors();
		model.addAttribute("authors", allAuthors);
		return "authors";
	}
	
	@GetMapping("/update/{id}")
	public String updateAuthor(Model model, @PathVariable Long id){
		AuthorDto authorDto = service.selAuthorById(id);
		AuthorSlimDto author = new AuthorSlimDto(id, authorDto.getFirstName(), authorDto.getLastName());
		model.addAttribute("author", author);
		return "editAuthor";
	}
	
	@PostMapping("/update/saveAuthor")
	public String saveAuthor(@Valid @ModelAttribute("author") AuthorSlimDto author,  BindingResult result) {
		if (result.hasErrors()) {
            return "editAuthor";
        }
		service.saveAuthor(author);
		return "redirect:/library/author";
	}
	
	@GetMapping(value = "/name/{firstName}/{lastName}")
	public String findByName(Model model, @PathVariable("firstName") String firstName, @PathVariable String lastName) {
		AuthorDto author = service.findAuthorByNameAndSurname(firstName, lastName);
		model.addAttribute("authors", author);
		model.addAttribute("AllAuthors", true);
		return "authors";
	}
	
	@PostMapping(value="/name")
	public String findByNamePost(@RequestParam("firstNameFound") String first, @RequestParam("lastNameFound") String last) {
		return "redirect:/library/author/name/" + first + "/" + last;
	}
	
}
