package com.library.webapp.restController;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.security.SecurityConfig;
import com.library.webapp.service.LibraryService;

@WebMvcTest(controllers = BookRestController.class)
@Import(SecurityConfig.class)
class BookRestControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private LibraryService service;
	
	@Test
	void testGetAllBook() throws Exception {
		when(service.selAllBooks()).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "Title A", "Genre", "plot",
						LocalDate.parse("2000-01-01"), 100, Set.of(new AuthorSlimDto(1L, "FirstNameA", "LastNameA"))),
				new BookDto("978-88-111-1111-2", "Title B", "Genre", "plot",
						LocalDate.parse("2000-02-02"), 200, Set.of(new AuthorSlimDto(2L, "FirstNameB", "LastNameB")))
				));
		
		this.mockMvc.perform(get("/api/book"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$[0].title", is("Title A")))
					.andExpect(jsonPath("$[0].genre", is("Genre")))
					.andExpect(jsonPath("$[0].plot", is("plot")))
					.andExpect(jsonPath("$[0].publicationDate", is("2000-01-01")))
					.andExpect(jsonPath("$[0].numberPages", is(100)))
					.andExpect(jsonPath("$[0].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[0].authors.[0].firstName", is("FirstNameA")))
					.andExpect(jsonPath("$[0].authors.[0].lastName", is("LastNameA")))
					.andExpect(jsonPath("$[1].isbn", is("978-88-111-1111-2")))
					.andExpect(jsonPath("$[1].title", is("Title B")))
					.andExpect(jsonPath("$[1].genre", is("Genre")))
					.andExpect(jsonPath("$[1].plot", is("plot")))
					.andExpect(jsonPath("$[1].publicationDate", is("2000-02-02")))
					.andExpect(jsonPath("$[1].numberPages", is(200)))
					.andExpect(jsonPath("$[1].authors.[0].id", is(2)))
					.andExpect(jsonPath("$[1].authors.[0].firstName", is("FirstNameB")))
					.andExpect(jsonPath("$[1].authors.[0].lastName", is("LastNameB")));
	}

	@Test
	void testAllBook_NoFound() throws Exception {
		when(service.selAllBooks()).thenThrow(new NotFoundException("There is no book!"));
		
		this.mockMvc.perform(get("/api/book"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("There is no book!")));
	}

	@Test
	void testSelBookById() throws Exception {
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "plot", 
				LocalDate.parse("2000-01-01"), 100, Set.of(new AuthorSlimDto(1L, "FirstName", "LastName")));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		
		this.mockMvc.perform(get("/api/book/978-88-111-1111-1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$.title", is("Title")))
					.andExpect(jsonPath("$.genre", is("Genre")))
					.andExpect(jsonPath("$.plot", is("plot")))
					.andExpect(jsonPath("$.publicationDate", is("2000-01-01")))
					.andExpect(jsonPath("$.numberPages", is(100)))
					.andExpect(jsonPath("$.authors.[0].id", is(1)))
					.andExpect(jsonPath("$.authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$.authors.[0].lastName", is("LastName")));
	}
	
	@Test
	void testSelBookById_NoFound() throws Exception {
		when(service.selBookByIsbn("978-88-111-1111-1")).thenThrow(new NotFoundException("Book not present or wrong ISBN!"));
		
		this.mockMvc.perform(get("/api/book/978-88-111-1111-1"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("Book not present or wrong ISBN!")));
	}
	
	@Test
	void testFindBooksByGenre() throws Exception {
		Set<AuthorSlimDto> author = Set.of(new AuthorSlimDto(1l,"FirstName", "LastName"));
		when(service.findBooksByGenre("Genre")).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "Title1", "Genre", "plot", LocalDate.parse("2000-01-01"), 100, author),
				new BookDto("978-88-111-1111-2", "Title2", "Genre","plot", LocalDate.parse("2000-02-02"), 200, author)
				));
		
		this.mockMvc.perform(get("/api/book/genre/Genre"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$[0].title", is("Title1")))
					.andExpect(jsonPath("$[0].genre", is("Genre")))
					.andExpect(jsonPath("$[0].plot", is("plot")))
					.andExpect(jsonPath("$[0].publicationDate", is("2000-01-01")))
					.andExpect(jsonPath("$[0].numberPages", is(100)))
					.andExpect(jsonPath("$[0].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[0].authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$[0].authors.[0].lastName", is("LastName")))
					.andExpect(jsonPath("$[1].isbn", is("978-88-111-1111-2")))
					.andExpect(jsonPath("$[1].title", is("Title2")))
					.andExpect(jsonPath("$[1].genre", is("Genre")))
					.andExpect(jsonPath("$[1].plot", is("plot")))
					.andExpect(jsonPath("$[1].publicationDate", is("2000-02-02")))
					.andExpect(jsonPath("$[1].numberPages", is(200)))
					.andExpect(jsonPath("$[1].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[1].authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$[1].authors.[0].lastName", is("LastName")));
	}
	
	@Test
	void testFindBooksByGenre_NoFound() throws Exception {
		when(service.findBooksByGenre(anyString()))
			.thenThrow(new NotFoundException("There are no books with genre Genre!"));
		
		this.mockMvc.perform(get("/api/book/genre/Genre"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("There are no books with genre Genre!")));
	}
	
	@Test
	void testFindBooksAfterPublicationDate() throws Exception {
		Set<AuthorSlimDto> author = Set.of(new AuthorSlimDto(1l,"FirstName", "LastName"));
		when(service.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01"))).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "Title1", "Genre", "plot",LocalDate.parse("2000-02-01"), 
						100, author),
				new BookDto("978-88-111-1111-2", "Title2", "Genre", "plot",LocalDate.parse("2000-03-01"), 
						200, author)
				));
		
		this.mockMvc.perform(get("/api/book/afterdate/2000-01-01"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$[0].title", is("Title1")))
					.andExpect(jsonPath("$[0].genre", is("Genre")))
					.andExpect(jsonPath("$[0].plot", is("plot")))
					.andExpect(jsonPath("$[0].publicationDate", is("2000-02-01")))
					.andExpect(jsonPath("$[0].numberPages", is(100)))
					.andExpect(jsonPath("$[0].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[0].authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$[0].authors.[0].lastName", is("LastName")))
					.andExpect(jsonPath("$[1].isbn", is("978-88-111-1111-2")))
					.andExpect(jsonPath("$[1].title", is("Title2")))
					.andExpect(jsonPath("$[1].genre", is("Genre")))
					.andExpect(jsonPath("$[1].plot", is("plot")))
					.andExpect(jsonPath("$[1].publicationDate", is("2000-03-01")))
					.andExpect(jsonPath("$[1].numberPages", is(200)))
					.andExpect(jsonPath("$[1].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[1].authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$[1].authors.[0].lastName", is("LastName")));	
	}
	
	@Test
	void testFindBooksAfterPublicationDate_NoFound() throws Exception {
		when(service.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01")))
			.thenThrow(new NotFoundException("There are no books published after 2000-01-01!"));
		
		this.mockMvc.perform(get("/api/book/afterdate/2000-01-01"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("There are no books published after 2000-01-01!")));
	}
	
	@Test
	void testFindBooksByTitleContains() throws Exception {
		Set<AuthorSlimDto> author = Set.of(new AuthorSlimDto(1l,"FirstName", "LastName"));
		when(service.findBooksTitleContains("AA")).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "titleAA1", "Genre", "plot",LocalDate.parse("2000-01-01"), 100, author),
				new BookDto("978-88-111-1111-2", "titleAA2", "Genre", "plot" ,LocalDate.parse("2000-02-02"), 200, author)
				));
		
		this.mockMvc.perform(get("/api/book/title/AA"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].isbn", is("978-88-111-1111-1")))
					.andExpect(jsonPath("$[0].title", is("titleAA1")))
					.andExpect(jsonPath("$[0].genre", is("Genre")))
					.andExpect(jsonPath("$[0].plot", is("plot")))
					.andExpect(jsonPath("$[0].publicationDate", is("2000-01-01")))
					.andExpect(jsonPath("$[0].numberPages", is(100)))
					.andExpect(jsonPath("$[0].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[0].authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$[0].authors.[0].lastName", is("LastName")))
					.andExpect(jsonPath("$[1].isbn", is("978-88-111-1111-2")))
					.andExpect(jsonPath("$[1].title", is("titleAA2")))
					.andExpect(jsonPath("$[1].genre", is("Genre")))
					.andExpect(jsonPath("$[1].plot", is("plot")))
					.andExpect(jsonPath("$[1].publicationDate", is("2000-02-02")))
					.andExpect(jsonPath("$[1].numberPages", is(200)))
					.andExpect(jsonPath("$[1].authors.[0].id", is(1)))
					.andExpect(jsonPath("$[1].authors.[0].firstName", is("FirstName")))
					.andExpect(jsonPath("$[1].authors.[0].lastName", is("LastName")));
	}
	
	@Test
	void testFindBooksByTitleContains_NoFound() throws Exception {
		when(service.findBooksTitleContains(anyString()))
			.thenThrow(new NotFoundException("There are no books with AA in the title!"));
		
		this.mockMvc.perform(get("/api/book/title/AA"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code", is(404)))
					.andExpect(jsonPath("$.message", is("There are no books with AA in the title!")));
	}

}
