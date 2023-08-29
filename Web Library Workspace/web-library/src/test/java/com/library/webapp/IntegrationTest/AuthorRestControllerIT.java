package com.library.webapp.IntegrationTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import java.time.LocalDate;
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
class AuthorRestControllerIT {

	@LocalServerPort 
	private int port;
	
	@Autowired
	AuthorRepository authorRepository;
	
	@Autowired
	BookRepository bookRepository;
	
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
	void testGetAllAuthors() throws Exception {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null , "FirstName", "LastName", null);
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		
		given().get("/api/author")
			.then().statusCode(200)
			.body("$.size()", equalTo(1)) 
        	.body(	
        			"firstName", hasItems(author.getFirstName()),
        			"lastName", hasItems(author.getLastName()),
        			"books[0].isbn", hasItems(book.getIsbn()),
    				"books[0].title", hasItems(book.getTitle())
        			); 
	}
	
	@Test
	void testGetAllAuthors_NotFoundException() throws Exception {
		given().get("/api/author")
			.then().statusCode(404)
			.body("message", equalTo("There is no author!"));
	}
	
	
	@Test
	void testGetAuthorById() throws Exception {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", null);
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		
		given().get("/api/author/" + author.getId())
		.then().statusCode(200)
		.body(
				"firstName", equalTo(author.getFirstName()),
				"lastName", equalTo(author.getLastName()),
				"books[0].isbn", equalTo(book.getIsbn()),
				"books[0].title", equalTo(book.getTitle())
				);
		
	}
	
	@Test
	void testGetAuthorById_NotFoundException() throws Exception {
		given().get("/api/author/1")
			.then().statusCode(404)
			.body("message", equalTo("Author not present or wrong ID!"));
	}
	
	@Test
	void testFindAuthorByName() throws Exception {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", null);
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		
		given().get("/api/author/name/FirstName/LastName")
			.then().statusCode(200)
			.body(
					"firstName", equalTo(author.getFirstName()),
					"lastName", equalTo(author.getLastName()),
					"books[0].isbn", equalTo(book.getIsbn()),
					"books[0].title", equalTo(book.getTitle())
					);
		
	}
	
	@Test
	void testFindAuthorByName_NotFound() throws Exception {
		given().get("/api/author/name/FirstName/LastName")
			.then().statusCode(404)
			.body("message", equalTo("There is no author with the specified name."));
	}
	
}
