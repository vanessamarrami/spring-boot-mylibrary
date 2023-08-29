package com.library.webapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
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

import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;
import com.library.webapp.exceptions.DuplicateException;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.security.SecurityConfig;
import com.library.webapp.service.LibraryService;


@WebMvcTest(controllers = BookWebController.class)
@Import(SecurityConfig.class)
class BookWebControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private LibraryService service; 
	
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
	void testSuccessEndpointLibrary() throws Exception {
		mockMvc.perform(get("/library")).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@WithAnonymousUser
	void testReturnBooksView() throws Exception {
		ModelAndViewAssert
		.assertViewName(mockMvc.perform(get("/library"))
				.andReturn().getModelAndView(), "books" );
	}

	@Test
	@WithAnonymousUser
	void testReturnListBooks_BooksView() throws Exception {
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot",
				LocalDate.parse("2000-01-01"), 100, Set.of(new AuthorSlimDto(1L, "FirstName", "LastName"))));
		
		when(service.selAllBooks()).thenReturn(books);
		mockMvc.perform(get("/library"))
			.andExpect(view().name("books"))
			.andExpect(model().attribute("books", books));
	}
	
	@Test
	@WithAnonymousUser
	void testNoBooks_ErrorView() throws Exception {
		when(service.selAllBooks())
			.thenThrow(new NotFoundException("There is no book!"));
		
		mockMvc.perform(get("/library"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "There is no book!"));
	}
	
	@Test
	@WithAnonymousUser
	void testReturnDetailsBookView() throws Exception{
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.parse("2000-01-01"),
				100, Set.of(new AuthorSlimDto(1L, "FirstName", "LastName")));
		
		when(service.selBookByIsbn("978-88-111-1111-1"))
			.thenReturn(book);
		
		mockMvc.perform(get("/library/978-88-111-1111-1"))
			.andExpect(view().name("detBook"))
			.andExpect(model().attribute("book", book));
	}
	
	@Test
	@WithAnonymousUser
	void testBookNoFound_ErrorView() throws Exception{
		when(service.selBookByIsbn("978-88-111-1111-1"))
			.thenThrow(new NotFoundException("Book not present or wrong ISBN!"));
		
		mockMvc.perform(get("/library/978-88-111-1111-1"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "Book not present or wrong ISBN!"));
	}
	
	@Test
	@WithUserDetails("admin")
	void testDeleteBook() throws Exception {
		when(service.delBook("978-88-111-1111-1")).thenReturn(true);
		
		mockMvc.perform(get("/library/delete/978-88-111-1111-1"))
			.andExpect(view().name("redirect:/library"));
		
		verify(service).delBook("978-88-111-1111-1");
	}
	
	@Test
	@WithUserDetails("admin")
	void testDeleteBook_ErrorView() throws Exception {
		when(service.delBook("978-88-111-1111-1"))
			.thenThrow(new NotFoundException("It is not possible to delete the book with ISBN 978-88-111-1111-1 because it is not present!"));
		
		mockMvc.perform(get("/library/delete/978-88-111-1111-1"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "It is not possible to delete the book with ISBN 978-88-111-1111-1 because it is not present!"));
	}
	
	@Test
	@WithUserDetails("admin")
	void testInsertNewBookView() throws Exception {
		BookDto bookDto = new BookDto();
		String authorNames = "";
		
		mockMvc.perform(get("/library/insert"))
			.andExpect(view().name("editBook"))
			.andExpect(model().attribute("message", "New Book"))
			.andExpect(model().attribute("authorNames", authorNames))
			.andExpect(model().attribute("bookDto", bookDto));
		
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@WithUserDetails("admin")
	void testInsertNewBook_RedirectBooksView() throws Exception {
		BookDto bookDto = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100,
				Set.of(new AuthorSlimDto(1L, "FistName", "LastName")));
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(new Author(1L, "FistName", "LastName", null)));
		
		when(service.fillAuthorsSetByStringName("FirstName LastName"))
			.thenReturn(Set.of(new AuthorSlimDto(1L, "FistName", "LastName")));
		when(service.insertNewBook(bookDto)).thenReturn(book);
		
		mockMvc.perform(post("/library/insert/saveNewBook").with(csrf())
		.param("isbn", "978-88-111-1111-1")
		.param("title", "Title")
		.param("genre", "Genre")
		.param("plot", "Plot")
		.param("numberPages", "100")
		.param("publicationDate", "2000-01-01")
		.param("authorNames", "FirstName LastName")
		)
		.andExpect(view().name("redirect:/library"))
		.andExpect(model().errorCount(0));
		
		verify(service).insertNewBook(bookDto);
	}
	
	@Test
	@WithUserDetails("admin")
	void testInsertNewBook_invalidInputBook() throws Exception {
		
		mockMvc.perform(post("/library/insert/saveNewBook").with(csrf())
				.param("isbn", "isbn")
				.param("title", "")
				.param("genre", "")
				.param("plot", "")
				.param("numberPages", "0")
				.param("publicationDate", "")
				.param("authorNames", "FirstName LastName")
				)
				.andExpect(view().name("editBook"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "isbn", "IsbnCodeConstraint"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "title", "NotEmpty"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "genre", "NotEmpty"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "plot", "Size"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "numberPages", "Min"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "publicationDate", "NotNull"));
	}
	
	@Test
	@WithUserDetails("admin")
	void testInsertNewBook_ErrorView() throws Exception {
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01),
				100, null);
		when(service.insertNewBook(book))
			.thenThrow(new DuplicateException("The book with ISBN 978-88-111-1111-1 is already there!"));
		
		mockMvc.perform(post("/library/insert/saveNewBook").with(csrf())
		.param("isbn", "978-88-111-1111-1")
		.param("title", "Title")
		.param("genre", "Genre")
		.param("plot", "Plot")
		.param("numberPages", "100")
		.param("publicationDate", "2000-01-01")
		.param("authorNames", "FirstName LastName")
		)
		.andExpect(view().name("errorPage"))
		.andExpect(model().attribute("errorMessage", "The book with ISBN 978-88-111-1111-1 is already there!"));
		
		verify(service).insertNewBook(book);
	}
	
	@Test
	@WithUserDetails("admin")
	void testUpdateBook() throws Exception {
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(new AuthorSlimDto(1l, "FirstName", "LastName")));
		String authorNames = "FirstName LastName";
		
		when(service.fillStringNameBySet(book.getAuthors())).thenReturn(authorNames);
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		
		mockMvc.perform(get("/library/update/978-88-111-1111-1"))
			.andExpect(view().name("editBook"))
			.andExpect(model().attribute("message", "Edit Book"))
			.andExpect(model().attribute("authorNames", authorNames))
			.andExpect(model().attribute("bookDto", book));
	}
	
	
	@Test
	@WithUserDetails("admin")
	void testSaveBook_RedirectViewBooks() throws Exception {
		Set<AuthorSlimDto> author = Set.of(new AuthorSlimDto(1l, "FirstName", "LastName"));
		BookDto book = new BookDto("978-88-111-1111-1", "New title", "Genre", "Plot", LocalDate.of(2000, 01, 01),
				100, author);
		String authorNames = "FirstName LastName";
		
		when(service.fillAuthorsSetByStringName(authorNames))
			.thenReturn(author);
		when(service.saveBook(book)).thenReturn(true);
		
		mockMvc.perform(post("/library/update/saveBook").with(csrf())
				.param("isbn", "978-88-111-1111-1")
				.param("title", "New title")
				.param("genre", "Genre")
				.param("plot", "Plot")
				.param("numberPages", "100")
				.param("publicationDate", "2000-01-01")
				.param("authorNames", "FirstName LastName")
				)
				.andExpect(view().name("redirect:/library"))
				.andExpect(model().errorCount(0));;
				
				verify(service).saveBook(book);
	}
	
	@Test
	@WithUserDetails("admin")
	void testSaveBook_invalidInputBook() throws Exception {
		
		mockMvc.perform(post("/library/update/saveBook").with(csrf())
				.param("isbn", "isbn")
				.param("title", "")
				.param("genre", "")
				.param("plot", "")
				.param("numberPages", "0")
				.param("publicationDate", "")
				.param("authorNames", "FirstName LastName")
				)
				.andExpect(view().name("editBook"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "isbn", "IsbnCodeConstraint"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "title", "NotEmpty"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "genre", "NotEmpty"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "plot", "Size"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "numberPages", "Min"))
				.andExpect(model().attributeHasFieldErrorCode("bookDto", "publicationDate", "NotNull"));
	}
	
	
	@Test
	@WithAnonymousUser
	void testFindBooksByGenre() throws Exception {
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "title", "genreBook", "plot", LocalDate.parse("2000-01-01"), 100, 
				Set.of(new AuthorSlimDto(1L, "firstName", "lastName"))));
		when(service.findBooksByGenre("genreBook")).thenReturn(books);
		
		mockMvc.perform(get("/library/genre/genreBook"))
			.andExpect(view().name("books"))
			.andExpect(model().attribute("books", books));
	}
	
	@Test
	@WithAnonymousUser
	void testFindBooksByGenre_ErrorPage() throws Exception {
		when(service.findBooksByGenre("genreBook"))
			.thenThrow(new NotFoundException("There are no books with genre genreBook!"));
		
		mockMvc.perform(get("/library/genre/genreBook"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "There are no books with genre genreBook!"));	
	}
	
	@Test
	@WithAnonymousUser
	void testFindBooksByDate() throws Exception {
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "Title", "GenreBook", "Plot", 
				LocalDate.parse("2001-01-01"), 100, Set.of(new AuthorSlimDto(1L, "FirstName", "LastName"))));
		
		when(service.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01")))
			.thenReturn(books);
		
		mockMvc.perform(get("/library/afterDate/2000-01-01"))
			.andExpect(view().name("books"))
			.andExpect(model().attribute("books", books));
	}
	
	@Test
	@WithAnonymousUser
	void testFindBooksByDate_ErrorPage() throws Exception {
		when(service.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01")))
			.thenThrow(new NotFoundException("There are no books published after 2001-01-01!"));
		
		mockMvc.perform(get("/library/afterDate/2000-01-01"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "There are no books published after 2001-01-01!"));	
	}
	
	@Test
	@WithAnonymousUser
	void testFindBooksByTitle() throws Exception{
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "TitleBook", "GenreBook", "Plot", 
				LocalDate.parse("2001-01-01"), 100, Set.of(new AuthorSlimDto(1L, "FirstName", "LastName"))));
		
		when(service.findBooksTitleContains("TitleBook"))
			.thenReturn(books);
		
		mockMvc.perform(get("/library/title/TitleBook"))
			.andExpect(view().name("books"))
			.andExpect(model().attribute("books", books))
			.andExpect(model().attribute("AllBooks", true));
	}
	
	@Test
	@WithAnonymousUser
	void testFindBooksByTitle_ErrorPage() throws Exception{
		when(service.findBooksTitleContains("TitleBook"))
			.thenThrow(new NotFoundException("There are no books with TitleBook in the title!"));
		
		mockMvc.perform(get("/library/title/TitleBook"))
			.andExpect(view().name("errorPage"))
			.andExpect(model().attribute("errorMessage", "There are no books with TitleBook in the title!"));
	}
	
	@Test
	@WithAnonymousUser
	void testFindBooksByTitle_Post() throws Exception {
		mockMvc.perform(post("/library/title").with(csrf())
				.param("titleFound", "titleToFound")
				)
				.andExpect(view().name("redirect:/library/title/titleToFound"));
	}
}
