package com.library.webapp.restController;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.service.LibraryService;

@RequestMapping("/api/author")
@RestController 
public class AuthorRestController {
	
	private Logger log = Logger.getLogger(AuthorRestController.class.getName());
	
	@Autowired
	private LibraryService service;
	
	@GetMapping
	public List<AuthorDto>listAllAuthor(){
		log.info("***** Get all authors. *****"); 
		List<AuthorDto> allAuthors = service.selAllAuthors();
		return allAuthors; 
	} 

	@GetMapping(value = "/{id}")
	public AuthorDto getById(@PathVariable("id") Long id){
		log.info("***** Get the author with ID: "+ id + "*****");
		AuthorDto author = service.selAuthorById(id);
		return author;
	}
	
	@GetMapping(value = "/name/{firstName}/{lastName}")
	public AuthorDto listAuthorByNameAndSurname(@PathVariable("firstName") String firstName, @PathVariable("lastName") String lastName){
		log.info("***** Search for an author by name. *****");
		AuthorDto author = service.findAuthorByNameAndSurname(firstName, lastName);
		return author;
	}
}
