package com.library.webapp.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;
import com.library.webapp.repository.AuthorRepository;
import com.library.webapp.repository.BookRepository;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookWebControllerIT {
	
	@LocalServerPort
	private int port;
	
	private String url; 
	
	private WebDriver driver;
	
	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	AuthorRepository authorRepository;
	
	@Container
	@ServiceConnection
	private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
	          .withDatabaseName("test")
	          .withUsername("user")
	          .withPassword("pass");
	
	@BeforeEach
	void init() {
		url = "http://localhost:" + port + "/library";
		driver = new HtmlUnitDriver();
		bookRepository.deleteAll();
		bookRepository.flush();
		authorRepository.deleteAll();
		authorRepository.flush();
	}

	@Test
	void testTableInBooksPage() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url);
		assertThat(driver.findElement(By.id("booksTable")).getText())
			.contains("Isbn Title Genre Authors \n"
					+ "978-88-111-1111-1 Title Genre FirstName LastName  ");
		driver.quit();
	}
	
	@Test
	void testTableEmptyBooksPage_Admin() {
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonBooks")).click();
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There is no book!");
		assertThat(driver.getPageSource()).doesNotContain("Login");
		assertEquals(driver.findElement(By.cssSelector("button")).getText(), "Insert your first book!");
		driver.quit();
	}
	
	@Test
	void testTableEmptyBooksPage_NotAuthenticated() {
		driver.get(url);
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There is no book!");
		assertThat(driver.findElement(By.id("buttonLogin")).getText())
			.contains("Login");
		assertThat(driver.getPageSource()).doesNotContain("Insert your first book!");
		driver.quit();
	}
	
	@Test
	void testElementsInDetailsPage() throws InterruptedException {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url + "/978-88-111-1111-1");
	
		assertEquals(driver.findElement(By.id("isbnBook")).getText(), "ISBN: 978-88-111-1111-1");
		assertEquals(driver.findElement(By.id("titleBook")).getText(), "Title");
		assertEquals(driver.findElement(By.id("genreBook")).getText(), "Genre: Genre");
		assertEquals(driver.findElement(By.id("authorsBook")).getText(), "FirstName LastName" );
		assertEquals(driver.findElement(By.id("dettPlotBook")).getText(),"Plot" );
		assertEquals(driver.findElement(By.id("publicationDateBook")).getText(),"Publication Date: 2000-01-01" );
		assertEquals(driver.findElement(By.id("numberPages")).getText(), "Number of Pages: 100");
		driver.quit();
	}
	
	@Test
	void testgetBook_NotFound() {
		driver.get(url + "/978-88-111-1111-1");
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("Book not present or wrong ISBN!");
		driver.quit();
	}

	@Test
	void testDeleteBook_Table() throws InterruptedException {
		Book bookTodelete = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.of(2000, 01, 01), 100, null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(bookTodelete));
		bookTodelete.setAuthors(Set.of(author));
		bookRepository.save(bookTodelete);
		
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonBooks")).click();
		driver.findElement(By.cssSelector("[href='/library/delete/978-88-111-1111-1']"))
			.click();
		assertEquals(driver.findElement(By.id("errMessage")).getText(), 
				"There is no book!");
		driver.quit();
	}
	
	@Test
	void testDeleteBook_DetBook() throws InterruptedException {
		Book bookTodelete = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.of(2000, 01, 01), 100, null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(bookTodelete));
		bookTodelete.setAuthors(Set.of(author));
		bookRepository.save(bookTodelete);
		
        driver.get(url);
        driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.get(url + "/978-88-111-1111-1");
		driver.findElement(By.cssSelector("[href='/library/delete/978-88-111-1111-1']"))
			.click();
		assertEquals(driver.findElement(By.id("errMessage")).getText(), 
				"There is no book!");
		driver.quit();
	}
	
	@Test
	void testDeleteBook_NotFound() {
		driver.get(url + "/delete/978-88-111-1111-1");
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("It is not possible to delete the book with ISBN 978-88-111-1111-1 because it is not present!");
		driver.quit();
	}
	
	@Test
	void testInsertInErrorPage_TableEmpty() {
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonBooks")).click();
		assertEquals(driver.findElement(By.id("errMessage")).getText(), 
				"There is no book!");
		driver.findElement(By.cssSelector("[href='/library/insert']"))
			.click();
		assertEquals(driver.getTitle(), "New Book");
		driver.findElement(By.id("isbn")).sendKeys("978-88-111-1111-1");
		driver.findElement(By.id("title")).sendKeys("Title Book");
		driver.findElement(By.id("genre")).sendKeys("Crime");
		driver.findElement(By.id("publicationDate")).sendKeys("2000-01-01");
		driver.findElement(By.id("numberPages")).sendKeys("100");
		driver.findElement(By.id("plot")).sendKeys("Plot Book");
		driver.findElement(By.name("authorNames")).sendKeys("Firstname Lastname");
		driver.findElement(By.name("submitButton")).click();
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
		"Isbn Title Genre Authors New Book\n"
		+ "978-88-111-1111-1 Title Book Crime Firstname Lastname  ");
		driver.quit();
	}
	
	@Test
	void testInsert_Table() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", 
				LocalDate.of(2000, 01, 01), 100, null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonBooks")).click();
		driver.findElement(By.cssSelector("[href='/library/insert']")).click();
		
		assertEquals(driver.getTitle(), "New Book");
		driver.findElement(By.id("isbn")).sendKeys("978-88-111-1111-2");
		driver.findElement(By.id("title")).sendKeys("Title 2");
		driver.findElement(By.id("genre")).sendKeys("Genre");
		driver.findElement(By.id("publicationDate")).sendKeys("2001-01-01");
		driver.findElement(By.id("numberPages")).sendKeys("150");
		driver.findElement(By.id("plot")).sendKeys("Plot");
		driver.findElement(By.name("authorNames")).sendKeys("FirstnameB LastnameB");
		driver.findElement(By.name("submitButton")).click();
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
		"Isbn Title Genre Authors New Book\n"
		+ "978-88-111-1111-1 Title Genre FirstName LastName  \n"
		+ "978-88-111-1111-2 Title 2 Genre FirstnameB LastnameB  ");
		driver.quit();
	}
	
	@Test
	void testInsertBook_InvalidInput() {
        driver.get(url + "/insert");
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
		assertEquals(driver.getTitle(), "New Book");
		driver.findElement(By.id("isbn")).sendKeys("");
		driver.findElement(By.id("title")).sendKeys("");
		driver.findElement(By.id("genre")).sendKeys("");
		driver.findElement(By.id("publicationDate")).sendKeys("");
		driver.findElement(By.id("numberPages")).sendKeys("");
		driver.findElement(By.id("plot")).sendKeys("");
		driver.findElement(By.name("authorNames")).sendKeys("FirstnameB LastnameB");
		driver.findElement(By.name("submitButton")).click();
		assertEquals(driver.getTitle(), "New Book");
		driver.quit();
	}
	
	@Test
	void testInsertBook_DuplicateExceptions() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url + "/insert");
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
      
		driver.findElement(By.id("isbn")).sendKeys("978-88-111-1111-1");
		driver.findElement(By.id("title")).sendKeys("Title");
		driver.findElement(By.id("genre")).sendKeys("Crime");
		driver.findElement(By.id("publicationDate")).sendKeys("2000-01-01");
		driver.findElement(By.id("numberPages")).sendKeys("100");
		driver.findElement(By.id("plot")).sendKeys("Plot");
		driver.findElement(By.name("authorNames")).sendKeys("Firstname Lastname");
		driver.findElement(By.name("submitButton")).click();
		
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("The book with ISBN 978-88-111-1111-1 is already there!");
		driver.quit();
	}
	
	@Test
	void testUpdate_DetPage() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot",
				LocalDate.of(2000, 01, 01), 100, null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.get(url + "/978-88-111-1111-1");
		driver.findElement(By.cssSelector("[href='/library/update/978-88-111-1111-1']"))
			.click();
		assertEquals(driver.getTitle(), "Edit Book");
		
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.id("title")).sendKeys("New Title");
		driver.findElement(By.name("authorNames")).clear();
		driver.findElement(By.name("authorNames")).sendKeys("NewFirstName LastName");
		driver.findElement(By.name("submitButton")).click();
		
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
		"Isbn Title Genre Authors New Book\n"
		+ "978-88-111-1111-1 New Title Crime NewFirstName LastName  ");
	
		driver.quit();
	}
	
	@Test
	void testUpdate_Table() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonBooks")).click();
		driver.findElement(By.cssSelector("[href='/library/update/978-88-111-1111-1']"))
			.click();
		assertEquals(driver.getTitle(), "Edit Book");
		
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.id("title")).sendKeys("New Title");
		driver.findElement(By.name("authorNames")).clear();
		driver.findElement(By.name("authorNames")).sendKeys("NewFirstName LastName");
		driver.findElement(By.name("submitButton")).click();
		
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
		"Isbn Title Genre Authors New Book\n"
		+ "978-88-111-1111-1 New Title Genre NewFirstName LastName  ");
	
		driver.quit();
	}
	
	@Test
	void testUpdate_Invalid() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url + "/update/978-88-111-1111-1");
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
		assertEquals(driver.getTitle(), "Edit Book");
		driver.findElement(By.id("title")).clear();
		driver.findElement(By.name("authorNames")).clear();
		driver.findElement(By.name("authorNames")).sendKeys("NewFirstName LastName");
		driver.findElement(By.name("submitButton")).click();
		assertEquals(driver.getTitle(), "Edit Book");
		driver.quit();
	}
	
	@Test
	void findBooksByGenre(){
		Book book1 = new Book("978-88-111-1111-1", "Title1", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Book book2 = new Book("978-88-111-1111-2", "Title2", "Novel", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author1 = new Author(null, "FirstNameA", "LastNameA", Set.of(book1));
		Author author2 = new Author(null, "FirstNameB", "LastNameB", Set.of(book2));
		book1.setAuthors(Set.of(author1));
		book2.setAuthors(Set.of(author2));
		bookRepository.saveAllAndFlush(List.of(book1, book2));
		driver.get(url);
		driver.findElement(By.id("menuGenre")).click();
		driver.findElement(By.cssSelector("[href='/library/genre/Crime']"))
			.click();
		
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
				"Isbn Title Genre Authors \n"
				+ "978-88-111-1111-1 Title1 Crime FirstNameA LastNameA  ");
		driver.quit();
	}
	
	@Test
	void testFindBooksByGenre_NotFound() {
		driver.get(url + "/genre/Crime");
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There are no books with genre Crime!");
		driver.quit();
	}
	
	@Test
	void findBooksByDate(){
		Book book1 = new Book("978-88-111-1111-1", "Title1", "Genre", "Plot", LocalDate.of(2010, 10, 01), 100, 
				null);
		Book book2 = new Book("978-88-111-1111-2", "Title2", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author1 = new Author(null, "FirstNameA", "LastNameA", Set.of(book1));
		Author author2 = new Author(null, "FirstNameB", "LastNameB", Set.of(book2));
		book1.setAuthors(Set.of(author1));
		book2.setAuthors(Set.of(author2));
		bookRepository.saveAllAndFlush(List.of(book1, book2));
		driver.get(url);
        
		driver.findElement(By.id("menuDate")).click();
		driver.findElement(By.cssSelector("[href='/library/afterDate/2010-01-01']"))
			.click();
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
				"Isbn Title Genre Authors \n"
				+ "978-88-111-1111-1 Title1 Genre FirstNameA LastNameA  ");
		driver.quit();
	}
	
	@Test
	void testFindBooksByDate_NotFound() {
		driver.get(url + "/afterDate/2010-01-01");
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There are no books published after 2010-01-01!");
		driver.quit();
	}
	
	@Test
	void findBooksByTitle(){
		Book book1 = new Book("978-88-111-1111-1", "Title1", "Genre", "Plot", LocalDate.of(2010, 10, 01), 100, 
				null);
		Book book2 = new Book("978-88-111-1111-2", "Title2", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author1 = new Author(null, "FirstNameA", "LastNameA", Set.of(book1));
		Author author2 = new Author(null, "FirstNameB", "LastNameB", Set.of(book2));
		book1.setAuthors(Set.of(author1));
		book2.setAuthors(Set.of(author2));
		bookRepository.saveAllAndFlush(List.of(book1, book2));
		driver.get(url);
		driver.findElement(By.name("titleFound")).sendKeys("Title1");
		driver.findElement(By.id("buttonTitle")).click();
		assertEquals(driver.findElement(By.id("booksTable")).getText(),
				"Isbn Title Genre Authors \n"
				+ "978-88-111-1111-1 Title1 Genre FirstNameA LastNameA  ");
		driver.quit();
	}
	
	@Test
	void testFindBooksByTitle_NotFound() {
		Book book1 = new Book("978-88-111-1111-1", "Title1", "Genre", "Plot", LocalDate.of(2010, 10, 01), 100, 
				null);
		Author author1 = new Author(null, "FirstNameA", "LastNameA", Set.of(book1));
		book1.setAuthors(Set.of(author1));
		bookRepository.save(book1);
		driver.get(url);
		driver.findElement(By.name("titleFound")).sendKeys("Title2");
		driver.findElement(By.id("buttonTitle")).click();
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There are no books with Title2 in the title!");
		driver.quit();
	}

}
