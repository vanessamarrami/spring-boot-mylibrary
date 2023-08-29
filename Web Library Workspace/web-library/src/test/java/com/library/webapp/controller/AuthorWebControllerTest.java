package com.library.webapp.controller;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookSlimDto;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.security.SecurityConfig;
import com.library.webapp.service.LibraryService;


@WebMvcTest(AuthorWebController.class)
@Import(SecurityConfig.class)
class AuthorWebControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	LibraryService service;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@BeforeEach
	public void setup() {
		MockMvcBuilders
	            .webAppContextSetup(webApplicationContext)
	            .apply(springSecurity())
	            .build();
	}
	
	@Test
	@WithAnonymousUser
	void testSuccessEndpointAuthors() throws Exception {
		mockMvc.perform(get("/library/author")).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@WithAnonymousUser
	void testReturnAuthorsView() throws Exception {
		ModelAndViewAssert
			.assertViewName(mockMvc.perform(get("/library/author"))
				.andReturn().getModelAndView(), "authors");
	}
	
	@Test
	@WithAnonymousUser
	void testReturnListAllAuthors() throws Exception {
		List<AuthorDto> allAuthors = List.of(new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "Title"))));
		when(service.selAllAuthors()).thenReturn(allAuthors);
		
		mockMvc.perform(get("/library/author"))
			.andExpect(view().name("authors"))
			.andExpect(model().attribute("authors", allAuthors));
		
	}
	
	@Test
	@WithAnonymousUser
	void testReturnListAllAuthors_ErrorPage() throws Exception {
		when(service.selAllAuthors()).thenThrow(new NotFoundException("There is no author!"));
		
		mockMvc.perform(get("/library/author"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "There is no author!"));
	}
	
	@Test
	@WithUserDetails("admin")
	void testUpdateAuthor() throws Exception {
		AuthorDto author = new AuthorDto(1L, "FirstName", "LastName", 
				Set.of(new BookSlimDto("978-88-111-1111-1", "Title")));
		AuthorSlimDto authorSlim = new AuthorSlimDto(1L, "FirstName", "LastName");
		when(service.selAuthorById(1L)).thenReturn(author);
		
		mockMvc.perform(get("/library/author/update/1"))
			.andExpect(view().name("editAuthor"))
			.andExpectAll(model().attribute("author", authorSlim));
	}
	
	@Test
	@WithUserDetails("admin")
	void testUpdateAuthor_Post() throws Exception {
		AuthorSlimDto authorSlim = new AuthorSlimDto(1L, "FirstName", "LastName");
		when(service.saveAuthor(authorSlim)).thenReturn(true);
		
		mockMvc.perform(post("/library/author/update/saveAuthor").with(csrf())
				.param("id", "1")
				.param("firstName", "FirstName")
				.param("lastName", "LastName")
				)
		.andExpect(view().name("redirect:/library/author"))
		.andExpect(model().errorCount(0));
		verify(service).saveAuthor(authorSlim);
	}
	
	@Test
	@WithUserDetails("admin")
	void testUpdateAuthorPost_InvalidInputAuthor() throws Exception {
		
		mockMvc.perform(post("/library/author/update/saveAuthor").with(csrf())
				.param("id", "1")
				.param("firstName", "")
				.param("lastName", "")
				)
				.andExpect(view().name("editAuthor"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrorCode("author", "firstName", "NotEmpty"))
				.andExpect(model().attributeHasFieldErrorCode("author", "lastName", "NotEmpty"));
	}
	
	@Test
	@WithAnonymousUser
	void testFindAuthorByName() throws Exception {
		AuthorDto author = new AuthorDto(1L , "FirstName", "LastName",
				Set.of(new BookSlimDto("978-88-111-1111-1", "title")));
		when(service.findAuthorByNameAndSurname("FirstName", "LastName")).thenReturn(author);
		
		mockMvc.perform(get("/library/author/name/FirstName/LastName"))
			.andExpect(view().name("authors"))
			.andExpect(model().attribute("authors", author))
			.andExpect(model().attribute("AllAuthors", true));
	}
	
	@Test
	@WithAnonymousUser
	void testFindAuthorByName_ErrorPage() throws Exception {
		when(service.findAuthorByNameAndSurname("FirstName", "LastName" ))
			.thenThrow(new NotFoundException("There is no author with the specified name."));
	
	mockMvc.perform(get("/library/author/name/FirstName/LastName"))
		.andExpect(view().name("errorPage"))
		.andExpect(model().attribute("errorMessage", "There is no author with the specified name."));
	}
	
	@Test
	@WithAnonymousUser
	void testFindAuthorByName_Post() throws Exception {
		mockMvc.perform(post("/library/author/name").with(csrf())
				.param("firstNameFound", "FirstName")
				.param("lastNameFound", "LastName")
				)
				.andExpect(view().name("redirect:/library/author/name/FirstName/LastName"));
	}
	
}
