package com.library.webapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.exceptions.DuplicateException;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.security.SecurityConfig;
import com.library.webapp.service.LibraryService;


@WebMvcTest(BookWebController.class)
@Import(SecurityConfig.class)
class BookWebControllerHtmlUnitTest {

	@Autowired
	private WebClient webClient;
	
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
	void testTitlePage() throws Exception{
		HtmlPage page = webClient.getPage("/library");
		assertEquals("Books", page.getTitleText());
	}
	
	@Test
	@WithAnonymousUser
	void testNavBarLinks() throws Exception{
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor homelink = page.getAnchorByText("Home");
		HtmlAnchor authorslink = page.getAnchorByText("List of Authors");
		HtmlAnchor loginlink = page.getAnchorByText("Login");
		assertEquals("/", homelink.getHrefAttribute());
		assertEquals("/library/author", authorslink.getHrefAttribute());
		assertEquals("/login", loginlink.getHrefAttribute());
	}
	
	@Test
	@WithUserDetails("admin")
	void testNavBarLinks_authenticated() throws Exception{
		HtmlPage page = webClient.getPage("/library");
		assertThat(page.getBody().getTextContent()).contains("Logout");
	}
	
	@Test
	@WithAnonymousUser
	void testTableBooks() throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		when(service.selAllBooks()).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "titleA", "Genre", "Plot", LocalDate.parse("2000-01-01"), 
						100, Set.of(author)),
				new BookDto("978-88-111-1111-2", "titleB", "Genre", "Plot", LocalDate.parse("2000-01-01"),
						100, Set.of(author))
				));
		
		HtmlPage page = webClient.getPage("/library");
		HtmlTable table = (HtmlTable) page.getElementById("booksTable");
		assertEquals(table.asNormalizedText(), 
				"Isbn	Title	Genre	Authors	\n"
				+ "978-88-111-1111-1	titleA	Genre	 FirstName LastName 	\n"
				+ "978-88-111-1111-2	titleB	Genre	 FirstName LastName 	");
	}
	
	@Test
	@WithUserDetails("admin")
	void testTableBooks_empty_InsertNewBookVisible() throws Exception{
		when(service.selAllBooks())
			.thenThrow(new NotFoundException("There is no book!"));
		
		HtmlPage page = webClient.getPage("/library");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There is no book!", page.getHtmlElementById("errMessage").getTextContent());
		
		assertThat(page.getBody().getTextContent()).doesNotContain("Go Back", "Login");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Insert your first book!");
	}
	
	@Test
	@WithAnonymousUser
	void testTableBooks_empty_InsertNotVisible() throws Exception{
		when(service.selAllBooks())
			.thenThrow(new NotFoundException("There is no book!"));
		
		HtmlPage page = webClient.getPage("/library");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There is no book!", page.getHtmlElementById("errMessage").getTextContent());
	
		assertThat(page.getBody().getTextContent()).doesNotContain("Go Back", "Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Login");
	}
	
	@Test
	@WithUserDetails("admin")
	void testExistenceButton_DetailsEditDelete_BooksTable() throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		when(service.selAllBooks()).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.parse("2000-01-01"), 
						100, Set.of(author))
				));
		
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor detBook = page.getAnchorByHref("/library/978-88-111-1111-1");
		HtmlAnchor editBook = page.getAnchorByHref("/library/update/978-88-111-1111-1");
		HtmlAnchor deleteBook = page.getAnchorByHref("/library/delete/978-88-111-1111-1");

		assertNotNull(detBook);
		assertNotNull(editBook);
		assertNotNull(deleteBook);
		assertThat(page.getBody().getTextContent()).doesNotContain("Back To All Books");
	}
	
	@Test
	@WithAnonymousUser
	void testExistenceButton_Details_butNotEditAndDeleteForAnonymous_BooksTable() throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		when(service.selAllBooks()).thenReturn(List.of(
				new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.parse("2000-01-01"),
						100, Set.of(author))
				));
		
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor detBook = page.getAnchorByHref("/library/978-88-111-1111-1");

		assertNotNull(detBook);
		assertThat(page.getBody().getTextContent()).doesNotContain(
				"Back To All Books",
				"/library/update/978-88-111-1111-1",
				"/library/delete/978-88-111-1111-1",
				"/library/insert"
				);
	}
	
	@Test
	@WithAnonymousUser
	void testGetDetailsBookPage_WhenClickTableButton() throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		when(service.selAllBooks()).thenReturn(List.of(book));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor detBook = page.getAnchorByHref("/library/978-88-111-1111-1");
		HtmlPage pageDetBook = detBook.click();
		assertEquals("Book Details", pageDetBook.getTitleText());
		assertThat(pageDetBook.getBody().getTextContent()).contains(
				"ISBN: 978-88-111-1111-1",
				"Title",
				"FirstName LastName",
				"Plot",
				"Genre: Genre",
				"Publication Date: 2000-01-01",
				"Number of Pages: 100");
		assertThat(pageDetBook.getBody().getTextContent()).contains("Home", "List of Authors", "List of Books");
		assertThat(pageDetBook.getBody().getTextContent()).doesNotContain("Edit", "Delete");
	}
	
	@Test
	@WithUserDetails("admin")
	void testElementsOfDetBookPage_Admin() throws Exception {
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot",
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		HtmlPage page = webClient.getPage("/library/978-88-111-1111-1");
		HtmlAnchor editlink = page.getAnchorByText("Edit");
		HtmlAnchor deletelink = page.getAnchorByText("Delete");
		
		assertEquals("/library/update/978-88-111-1111-1", editlink.getHrefAttribute());
		assertEquals("/library/delete/978-88-111-1111-1", deletelink.getHrefAttribute());
	}
	
	@Test
	@WithAnonymousUser
	void testDetBookPage_errorView() throws Exception {
		when(service.selBookByIsbn("978-88-111-1111-1"))
			.thenThrow(new NotFoundException("Book not present or wrong ISBN!"));
		
		HtmlPage page = webClient.getPage("/library/978-88-111-1111-1");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("Book not present or wrong ISBN!", page.getHtmlElementById("errMessage").getTextContent());
		
		assertThat(page.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithUserDetails("admin")
	void testDeleteBook_WhenClickTableButton() throws Exception {
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto bookTodelete = new BookDto("978-88-111-1111-1", "titleA", "genreA", "plotA",
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		when(service.selAllBooks()).thenReturn(List.of(bookTodelete));
		when(service.delBook("978-88-111-1111-1")).thenReturn(true);
		
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor deleteBook = page.getAnchorByHref("/library/delete/978-88-111-1111-1");
		HtmlPage pageDeleteBook = deleteBook.click();
		
		assertThat(pageDeleteBook.getUrl().getPath()).isEqualTo("/library");
		verify(service).delBook("978-88-111-1111-1");
	}
	
	@Test
	@WithUserDetails("admin")
	void testDeleteBook_WhenClickButtonDeleteInDetBook() throws Exception {
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto bookToDelete = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot",
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(bookToDelete);
		when(service.delBook("978-88-111-1111-1")).thenReturn(true);
		HtmlPage page = webClient.getPage("/library/978-88-111-1111-1");
		
		HtmlAnchor deletelink = page.getAnchorByText("Delete");
		HtmlPage pageDeleteBook = deletelink.click();
		assertThat(pageDeleteBook.getUrl().getPath()).isEqualTo("/library");
		verify(service).delBook("978-88-111-1111-1");
	}
	
	@Test
	@WithUserDetails("admin")
	void testDeleteBook_noFound() throws Exception {
		when(service.delBook("978-88-111-1111-1"))
			.thenThrow(new NotFoundException("It is not possible to delete the book with ISBN 978-88-111-1111-1 because it is not present!"));
		HtmlPage page = webClient.getPage("/library/delete/978-88-111-1111-1");
		
		assertEquals("Error Page", page.getTitleText());
		assertEquals("It is not possible to delete the book with ISBN 978-88-111-1111-1 because it is not present!", 
				page.getHtmlElementById("errMessage").getTextContent());
		
		assertThat(page.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithUserDetails("admin")
	void testGetInsertNewBookPage_WhenClickTableButton() throws Exception{
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor newBooklink = page.getAnchorByHref("/library/insert");
		HtmlPage pageDetBook = newBooklink.click();
		assertEquals("New Book", pageDetBook.getTitleText());
	}
	
	@Test
	@WithUserDetails("admin")
	void testNavBarLink_InsertNewBookPage() throws Exception{
		HtmlPage page = webClient.getPage("/library/insert");
		HtmlAnchor homelink = page.getAnchorByText("Home");
		HtmlAnchor booklink = page.getAnchorByText("List of Books");
		HtmlAnchor authorslink = page.getAnchorByText("List of Authors");
		
		assertEquals("/", homelink.getHrefAttribute());
		assertEquals("/library/author", authorslink.getHrefAttribute());
		assertEquals("/library", booklink.getHrefAttribute());
	}
	
	@Test
	@WithUserDetails("admin")
	void testinsertNewBook_validInput() throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		
		HtmlPage page = webClient.getPage("/library/insert");
		HtmlForm form = page.getFormByName("bookForm");
		HtmlTextInput isbn = page.getElementByName("isbn");
		isbn.setValue("978-88-111-1111-1");
		HtmlTextInput title = page.getElementByName("title");
		title.setValue("Title");
		HtmlTextInput genre = page.getElementByName("genre");
		genre.setValue("Genre");
		HtmlTextInput publicationDate = page.getElementByName("publicationDate");
		publicationDate.setValue("2000-01-01");
		HtmlNumberInput numberPages = page.getElementByName("numberPages");
		numberPages.setValue("100");
		HtmlTextArea plot = page.getElementByName("plot");
		plot.setText("Plot");
		HtmlTextInput authorNamesInput = page.getElementByName("authorNames");
        authorNamesInput.setValueAttribute("Firstname Lastname");
        HtmlInput submit = form.getInputByName("submitButton");
        HtmlPage resultPage = submit.click();
        assertEquals("/library", resultPage.getUrl().getPath());
        verify(service).insertNewBook(book);
	}
	
	@Test
	@WithUserDetails("admin")
	void testinsertNewBook_invalidInput() throws Exception{
		HtmlPage page = webClient.getPage("/library/insert");
		final HtmlForm form = page.getFormByName("bookForm");
		HtmlTextInput isbn = page.getElementByName("isbn");
		isbn.setValue("978-88-111-1111-1");
		
        HtmlInput submit = form.getInputByName("submitButton");
        HtmlPage resultPage = submit.click();
        assertEquals("/library/insert", resultPage.getUrl().getPath());
	}
	
	@Test
	@WithUserDetails("admin")
	void testinsertNewBook_errorPage() throws Exception{
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.of(2000, 01, 01), 100, null);
		when(service.insertNewBook(book))
			.thenThrow(new DuplicateException("The book with ISBN 978-88-111-1111-1 is already there!"));
		
		HtmlPage page = webClient.getPage("/library/insert");
		final HtmlForm form = page.getFormByName("bookForm");
		HtmlTextInput isbn = page.getElementByName("isbn");
		isbn.setValue("978-88-111-1111-1");
		HtmlTextInput title = page.getElementByName("title");
		title.setValue("Title");
		HtmlTextInput genre = page.getElementByName("genre");
		genre.setValue("Genre");
		HtmlTextInput publicationDate = page.getElementByName("publicationDate");
		publicationDate.setValue("2000-01-01");
		HtmlNumberInput numberPages = page.getElementByName("numberPages");
		numberPages.setValue("100");
		HtmlTextArea plot = page.getElementByName("plot");
		plot.setText("Plot");
		HtmlTextInput authorNamesInput = page.getElementByName("authorNames");
        authorNamesInput.setValueAttribute("Fistname Lastname");
        HtmlInput submit = form.getInputByName("submitButton");
        HtmlPage resultPage = submit.click();
        
		assertEquals("Error Page", resultPage.getTitleText());
		assertEquals("The book with ISBN 978-88-111-1111-1 is already there!", 
				resultPage.getHtmlElementById("errMessage").getTextContent());
		
		assertThat(resultPage.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(resultPage.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithUserDetails("admin")
	void testFormUpdateBook_WhenClickTableButton() throws Exception {
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot",
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		when(service.selAllBooks()).thenReturn(List.of(book));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		
		HtmlPage page = webClient.getPage("/library");
		HtmlAnchor editBook = page.getAnchorByHref("/library/update/978-88-111-1111-1");
		HtmlPage pageEditBook = editBook.click();
		assertEquals("Edit Book", pageEditBook.getTitleText());
		HtmlForm form = pageEditBook.getFormByName("bookForm");
		assertThat(form.getInputByName("isbn").getValueAttribute()).isNotNull();
		assertThat(form.getInputByName("title").getValueAttribute()).isNotNull();
		assertThat(form.getInputByName("genre").getValueAttribute()).isNotNull();
		assertThat(form.getInputByName("publicationDate").getValueAttribute()).isNotNull();
		assertThat(form.getInputByName("numberPages").getValueAttribute()).isNotNull();
		assertThat(form.getTextAreaByName("plot").getText()).isNotNull();
		assertThat(form.getInputByName("authorNames").getValueAttribute()).isNotNull();
	}
	
	@Test
	@WithUserDetails("admin")
	void testGetUpdateBookPage_WhenClickButtonEditInDetBook() throws Exception {
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.parse("2000-01-01"), 100, Set.of(author));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		HtmlPage page = webClient.getPage("/library/978-88-111-1111-1");
		
		HtmlAnchor editLink = page.getAnchorByText("Edit");
		HtmlPage pageEditBook = editLink.click();
		assertThat(pageEditBook.getUrl().getPath()).isEqualTo("/library/update/978-88-111-1111-1");
	}
	
	@Test
	@WithUserDetails("admin")
	void testEditBook_ValidInput()  throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "title", "genre", "plot", LocalDate.parse("2000-01-01"),
				100, Set.of(author));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		when(service.fillAuthorsSetByStringName("FirstName LastName")).thenReturn(Set.of(author));
		when(service.saveBook(book)).thenReturn(true);
		HtmlPage page = webClient.getPage("/library/update/978-88-111-1111-1");
		HtmlForm form = page.getFormByName("bookForm");
		form.getInputByValue("title").setValueAttribute("New Title");
		HtmlTextInput authorNamesInput = page.getElementByName("authorNames");
        authorNamesInput.setValueAttribute("Fistname Lastname");
        
        HtmlInput submit = form.getInputByName("submitButton");
        HtmlPage resultPage = submit.click();
		assertThat(resultPage.getUrl().getPath()).isEqualTo("/library");
	}
	
	@Test
	@WithUserDetails("admin")
	void testEditBook_InvalidInput()  throws Exception{
		AuthorSlimDto author = new AuthorSlimDto(1L, "FirstName", "LastName");
		BookDto book = new BookDto("978-88-111-1111-1", "title", "genre", "plot", LocalDate.parse("2000-01-01"),
				100, Set.of(author));
		when(service.selBookByIsbn("978-88-111-1111-1")).thenReturn(book);
		when(service.fillStringNameBySet(book.getAuthors())).thenReturn("FirstName LastName");
		when(service.fillAuthorsSetByStringName("FirstName LastName")).thenReturn(Set.of(author));
		HtmlPage page = webClient.getPage("/library/update/978-88-111-1111-1");
		final HtmlForm form = page.getFormByName("bookForm");
		form.getInputByValue("title").setValueAttribute("");
        HtmlInput submit = form.getInputByName("submitButton");
        HtmlPage resultPage = submit.click();
		assertThat(resultPage.getUrl().getPath()).isEqualTo("/library/update/saveBook");
	}
	
	@Test
	@WithAnonymousUser
	void testDropDown_menuGenre() throws Exception {
		HtmlPage page = webClient.getPage("/library");
		HtmlButton genre = (HtmlButton) page.getElementById("menuGenre");
		genre.click();
		HtmlAnchor selGenre = page.getAnchorByText("Fantasy");
		HtmlPage genrepage =  selGenre.click();
		
		assertThat(genrepage.getUrl().getPath()).isEqualTo("/library/genre/Fantasy");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByGenre() throws Exception {
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "Title", "Crime", "plot",
				LocalDate.parse("2000-01-01"), 100, 
				Set.of(new AuthorSlimDto(1L, "FirstName", "LastName"))));
		when(service.findBooksByGenre("Crime")).thenReturn(books);
		HtmlPage page = webClient.getPage("/library/genre/Crime");
		
		HtmlTable table = (HtmlTable) page.getElementById("booksTable");
		assertEquals(table.asNormalizedText(), 
				"Isbn	Title	Genre	Authors	\n"
				+ "978-88-111-1111-1	Title	Crime	 FirstName LastName 	");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByGenre_errorPage() throws Exception {
		when(service.findBooksByGenre("Crime"))
			.thenThrow(new NotFoundException("There are no books with genre Crime!"));
		HtmlPage page = webClient.getPage("/library/genre/Crime");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There are no books with genre Crime!", 
				page.getHtmlElementById("errMessage").getTextContent());

		assertThat(page.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithAnonymousUser
	void testDropDown_menuDate() throws Exception {
		HtmlPage page = webClient.getPage("/library");
		HtmlButton date = (HtmlButton) page.getElementById("menuDate");
		date.click();
		HtmlAnchor selDate = page.getAnchorByText("2000");
		HtmlPage datePage =  selDate.click();
		
		assertThat(datePage.getUrl().getPath()).isEqualTo("/library/afterDate/2000-01-01");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByPublicationDate() throws Exception {
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "Title", "genreBook", "Plot", 
				LocalDate.parse("2001-01-01"), 100, 
				Set.of(new AuthorSlimDto(1L, "FirstName", "LastName"))));
		when(service.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01")))
			.thenReturn(books);
		
		HtmlPage page = webClient.getPage("/library/afterDate/2000-01-01");
		HtmlTable table = (HtmlTable) page.getElementById("booksTable");
		assertEquals(table.asNormalizedText(), 
				"Isbn	Title	Genre	Authors	\n"
				+ "978-88-111-1111-1	Title	genreBook	 FirstName LastName 	");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByPublicationDate_ErrorPage() throws Exception {
		when(service.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01")))
			.thenThrow(new NotFoundException("There are no books published after 2000-01-01!"));
		
		HtmlPage page = webClient.getPage("/library/afterDate/2000-01-01");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There are no books published after 2000-01-01!",
				page.getHtmlElementById("errMessage").getTextContent());

		assertThat(page.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByTitle() throws Exception {
		List<BookDto> books = List.of(new BookDto("978-88-111-1111-1", "titleBook", "genreBook", "plot", LocalDate.parse("2001-01-01"), 100, 
				Set.of(new AuthorSlimDto(1L, "FirstName", "LastName"))));
		when(service.findBooksTitleContains("titleBook"))
			.thenReturn(books);
		HtmlPage page = webClient.getPage("/library");
		HtmlTextInput titleInput = page.getElementByName("titleFound");
		titleInput.setValue("titleBook");
		HtmlButton searchButton = (HtmlButton) page.getElementById("buttonTitle");
		HtmlPage result = searchButton.click();
		
		HtmlTable table = (HtmlTable) result.getElementById("booksTable");
		assertThat(result.getUrl().getPath()).isEqualTo("/library/title/titleBook");
		assertEquals(table.asNormalizedText(), 
				"Isbn	Title	Genre	Authors	\n"
				+ "978-88-111-1111-1	titleBook	genreBook	 FirstName LastName 	");
		
		assertThat(result.getBody().getTextContent()).contains("Back To All Books");
	}
	
	@Test
	@WithAnonymousUser
	void testFindByTitle_errorPage() throws Exception {
		when(service.findBooksTitleContains("titleBook"))
			.thenThrow(new NotFoundException("There are no books with titleBook in the title!"));
		
		HtmlPage page = webClient.getPage("/library/title/titleBook");
		assertEquals("Error Page", page.getTitleText());
		assertEquals("There are no books with titleBook in the title!", page.getHtmlElementById("errMessage").getTextContent());

		assertThat(page.getBody().getTextContent()).doesNotContain("Insert your first book!");
		assertThat(page.getBody().getTextContent()).contains("Go Home", "Go Back");
	}
	
	@Test
	@WithUserDetails("admin")
	void testlogout() throws Exception {
		HtmlPage page = webClient.getPage("/library");
		HtmlButton logout = (HtmlButton) page.getElementById("buttonLogout");
		HtmlPage home = logout.click();
		assertEquals(home.getTitleText(), "Library");
	}
}

