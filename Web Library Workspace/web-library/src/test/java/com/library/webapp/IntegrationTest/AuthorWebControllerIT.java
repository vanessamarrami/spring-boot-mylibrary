package com.library.webapp.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
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
class AuthorWebControllerIT {
	
	@LocalServerPort
	private int port;
	
	private String url;
	
	private WebDriver driver;
	
	@Autowired
	private AuthorRepository authorRepository;
	
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
		url = "http://localhost:" + port + "/library/author";
		driver = new HtmlUnitDriver();
		bookRepository.deleteAll();
		bookRepository.flush();
		authorRepository.deleteAll();
		authorRepository.flush();
	}	
	
	@Test
	void testSelAllAuthor() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", null);
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		driver.get(url);
		
		assertThat(driver.findElement(By.id("authorsTable")).getText())
			.contains("Id First Name Last Name Books\n"
					+ ""+author.getId()+" FirstName LastName 978-88-111-1111-1 Title");
		driver.quit();
	}
	
	@Test
	void testSelAllAuthor_NotFound_NotAuthorized() {
		driver.get(url);
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There is no author!");
		assertThat(driver.findElement(By.id("buttonLogin")).getText())
			.contains("Login");
		assertThat(driver.getPageSource()).doesNotContain("Insert your first book!");
		driver.quit();
	}
	
	@Test
	void testSelAllAuthor_NotFound_Admin() {
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonAuthors")).click();
		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There is no author!");
		assertEquals(driver.findElement(By.cssSelector("button")).getText(), "Insert your first book!");
		driver.quit();
	}
	
	@Test
	void testUpdate_Table() {
		Author author = new Author(null, "FirstName", "LastName", null);
		authorRepository.save(author);
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonAuthors")).click();
		driver.findElement(By.cssSelector(String.format("[href='/library/author/update/%d']", author.getId()))).click();
		assertEquals(driver.getTitle(), "Edit Author");
		
		driver.findElement(By.name("firstName")).clear();
		driver.findElement(By.name("firstName")).sendKeys("NewFirstName");
		driver.findElement(By.name("submitButton")).click();
		assertThat(driver.findElement(By.id("authorsTable")).getText())
		.contains("Id First Name Last Name Books \n"
				+ ""+author.getId()+" NewFirstName LastName  ");
		driver.quit();
	}

	@Test
	void testUpdate_Table_Invalid() {
		Author author = new Author(null, "FirstName", "LastName", null);
		authorRepository.save(author);
		driver.get(url);
		driver.findElement(By.id("buttonLogin")).click();
		driver.findElement(By.id("userId")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("adminPass");
        driver.findElement(By.id("submitlogin")).click();
        driver.findElement(By.id("buttonAuthors")).click();
		driver.findElement(By.cssSelector(String.format("[href='/library/author/update/%d']", author.getId()))).click();
		assertEquals(driver.getTitle(), "Edit Author");
		driver.findElement(By.name("firstName")).clear();
		driver.findElement(By.name("submitButton")).click();
		assertEquals(driver.getTitle(), "Edit Author");
		driver.quit();
	}
	
	@Test
	void findAuthorByName() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", null);
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		book.setAuthors(Set.of(author));
		driver.get(url);
		driver.findElement(By.name("firstNameFound")).sendKeys("FirstName");
		driver.findElement(By.name("lastNameFound")).sendKeys("LastName");
		driver.findElement(By.id("buttonName")).click();
		assertEquals(driver.findElement(By.id("authorsTable")).getText(),
				"Id First Name Last Name Books\n"
				+ ""+ author.getId()+" FirstName LastName ");
		driver.quit();
	}
	
	@Test
	void findAuthorByName_notFound() {
		Book book = new Book("978-88-111-1111-1", "Title", "Genre", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(null, "FirstName", "LastName", null);
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		book.setAuthors(Set.of(author));
		driver.get(url);
		driver.findElement(By.name("firstNameFound")).sendKeys("Name");
		driver.findElement(By.name("lastNameFound")).sendKeys("LastName");
		driver.findElement(By.id("buttonName")).click();

		assertThat(driver.findElement(By.id("errMessage")).getText())
			.contains("There is no author with the specified name.");
		driver.quit();
	}

}
