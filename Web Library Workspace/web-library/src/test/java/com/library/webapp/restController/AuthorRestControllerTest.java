package com.library.webapp.restController;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.BookSlimDto;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.security.SecurityConfig;
import com.library.webapp.service.LibraryService;

@WebMvcTest(controllers = AuthorRestController.class)
@Import(SecurityConfig.class)
class AuthorRestControllerTest{
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private LibraryService service;
	
	@Test
	void testGetAllAuthor() throws Exception {
		AuthorDto authorA = new AuthorDto(1L, "FirstnameA","LastNameA", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "TitleA")));
		AuthorDto authorB = new AuthorDto(2L, "FirstnameB", "LastNameB", 
				Set.of(new BookSlimDto("978-88-111-1111-2", "TitleB")));
		when(service.selAllAuthors()).thenReturn(List.of(authorA, authorB));
		
		this.mockMvc.perform(get("/api/author"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].id", is(1)))
					.andExpect(jsonPath("$[0].firstName", is("FirstnameA")))
					.andExpect(jsonPath("$[0].lastName", is("LastNameA")))
					.andExpect(jsonPath("$[0].books.[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$[0].books.[0].title", is("TitleA")))
					.andExpect(jsonPath("$[1].id", is(2)))
					.andExpect(jsonPath("$[1].firstName", is("FirstnameB")))
					.andExpect(jsonPath("$[1].lastName", is("LastNameB")))
					.andExpect(jsonPath("$[1].books.[0].isbn", is("978-88-111-1111-2")))
					.andExpect(jsonPath("$[1].books.[0].title", is("TitleB")));
	}

	
	@Test
	void testGetAllAuthor_NotFound() throws Exception {
		when(service.selAllAuthors()).thenThrow(new NotFoundException("There is no author!"));
		
		this.mockMvc.perform(get("/api/author"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("There is no author!")));
	} 

	@Test
	void testSelAuthorById() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName",  
				Set.of(new BookSlimDto("978-88-111-1111-1", "Title")));
		when(service.selAuthorById(1L)).thenReturn(author);
		
		this.mockMvc.perform(get("/api/author/1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id", is(1)))
					.andExpect(jsonPath("$.firstName", is("FirstName")))
					.andExpect(jsonPath("$.lastName", is("LastName")))
					.andExpect(jsonPath("$.books.[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$.books.[0].title", is("Title")));
	}
	
	@Test
	void testSelAuthorById_NotFound() throws Exception {
		when(service.selAuthorById(anyLong()))
			.thenThrow(new NotFoundException("Author not present or wrong ID!"));
		
		this.mockMvc.perform(get("/api/author/1"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("Author not present or wrong ID!")));
	}

	
	@Test
	void testFindAuthorByName() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "Title")));
		when(service.findAuthorByNameAndSurname("FirstName", "LastName")).thenReturn(author);
		
		this.mockMvc.perform(get("/api/author/name/FirstName/LastName"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id", is(1)))
					.andExpect(jsonPath("$.firstName", is("FirstName")))
					.andExpect(jsonPath("$.lastName", is("LastName")))
					.andExpect(jsonPath("$.books.[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$.books.[0].title", is("Title")));
	}
	
	@Test
	void testFindAuthorByName_NotFound() throws Exception {
		when(service.findAuthorByNameAndSurname(anyString(), anyString()))
			.thenThrow(new NotFoundException("There is no author with the specified name."));
		
		this.mockMvc.perform(get("/api/author/name/FirstName/LastName"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("There is no author with the specified name.")));
		
	}


}
