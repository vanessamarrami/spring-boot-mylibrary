package com.library.webapp.IntegrationTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given; 

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookRestControllerIT {

	@LocalServerPort
	private int port;

	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private AuthorRepository authorRepository;
	
	@Container
	@ServiceConnection
	private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
	          .withDatabaseName("test")
	          .withUsername("user")
	          .withPassword("pass");
	
	@BeforeEach
	void init() {
		RestAssured.port = port;
		bookRepository.deleteAll();
		bookRepository.flush();
		authorRepository.deleteAll();
		authorRepository.flush();
	} 
	
	@Test
	void testGetAllBooks() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		
		given().get("/api/book")
        	.then()
        	.statusCode(200)
        	.body("$.size()", equalTo(1)) 
        	.body("isbn", hasItems("978-88-111-1111-1")); 
	}
	
	@Test
	void testGetAllBooks_NotFound() throws Exception {
		given().get("/api/book")
			.then().statusCode(404)
			.body("message", equalTo("There is no book!"));
	}
	
	@Test
	void testGetBook() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		
		given().get("/api/book/978-88-111-1111-1")
			.then().statusCode(200)
			.body(
					"isbn", equalTo("978-88-111-1111-1"),
					"title", equalTo("Title"),
					"genre", equalTo("Genre"),
					"publicationDate", equalTo("2000-01-01"),
					"numberPages", equalTo(100),
					"authors[0].firstName", equalTo("FirstName"),
					"authors[0].lastName", equalTo("LastName")
					);
	}
	
	@Test
	void testGetBook_NotFound(){
		given().get("/api/book/978-88-111-1111-1")
			.then().statusCode(404)
			.body("message", equalTo("Book not present or wrong ISBN!"));
	}
	
	@Test
	void testFindBooksByGenre() {
		Book bookA = new Book("978-88-111-1111-1", "TitleA", "GenreBook", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Book bookB = new Book("978-88-111-1111-2", "TitleB", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author authorA = new Author(null, "FirstNameA", "LastNameA", Set.of(bookA));
		Author authorB = new Author(null, "FirstNameB", "LastNameB", Set.of(bookB));
		bookA.setAuthors(Set.of(authorA));
		bookB.setAuthors(Set.of(authorB));
		bookRepository.saveAllAndFlush(List.of(bookA, bookB));

		given().get("/api/book/genre/GenreBook")
			.then().statusCode(200)
			.body("$.size()", equalTo(1))
			.body("isbn", hasItems("978-88-111-1111-1"));
	}
	
	@Test
	void testFindBooksByGenre_NotFound() {
		given().get("/api/book/genre/GenreBook")
			.then().statusCode(404)
			.body("message", equalTo("There are no books with genre GenreBook!"));
	}
	
	@Test
	void testFindBookByPublicationDate() {
		Book bookA = new Book("978-88-111-1111-1", "TitleA", "Genre", "Plot", LocalDate.of(2000, 02, 01), 100, 
				null);
		Book bookB = new Book("978-88-111-1111-2", "TitleB", "Genre", "Plot", LocalDate.of(1999, 01, 01), 100, 
				null);
		Author authorA = new Author(null, "FirstNameA", "LastNameA", Set.of(bookA));
		Author authorB = new Author(null, "FirstNameB", "LastNameB", Set.of(bookB));
		bookA.setAuthors(Set.of(authorA));
		bookB.setAuthors(Set.of(authorB));
		bookRepository.saveAllAndFlush(List.of(bookA, bookB));
		
		given().get("/api/book/afterdate/2000-01-01")
			.then().statusCode(200)
			.body("$.size()", equalTo(1))
			.body("isbn", hasItems("978-88-111-1111-1"));
	}
	
	@Test
	void testFindBooksByPublicationDate_NotFound() {
		given().get("/api/book/afterdate/2010-01-01")
			.then().statusCode(404)
			.body("message", equalTo("There are no books published after 2010-01-01!"));
	}
	
	@Test
	void testFindBooksByTitle() {
		Book bookA = new Book("978-88-111-1111-1", "TitleA", "Genre", "Plot", LocalDate.of(2000, 02, 01), 100, 
				null);
		Book bookB = new Book("978-88-111-1111-2", "TitleB", "Genre", "Plot", LocalDate.of(1999, 01, 01), 100, 
				null);
		Author authorA = new Author(null, "FirstNameA", "LastNameA", Set.of(bookA));
		Author authorB = new Author(null, "FirstNameB", "LastNameB", Set.of(bookB));
		bookA.setAuthors(Set.of(authorA));
		bookB.setAuthors(Set.of(authorB));
		bookRepository.saveAllAndFlush(List.of(bookA, bookB));
		
		given().get("/api/book/title/TitleA")
			.then().statusCode(200)
			.body("$.size()", equalTo(1))
			.body("isbn", hasItems("978-88-111-1111-1"));
	}
	
	@Test
	void testFindBookByTitle_NotFound() {
		given().get("/api/book/title/TitleBook")
			.then().statusCode(404)
			.body("message", equalTo("There are no books with TitleBook in the title!"));
	}
}