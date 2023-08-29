package com.library.webapp.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.dtos.mapper.MapStructMapper;
import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;
import com.library.webapp.repository.AuthorRepository;
import com.library.webapp.repository.BookRepository;
import com.library.webapp.service.LibraryService;

@Testcontainers
@SpringBootTest
class LibraryServiceRepositoryIT {

	@Autowired
	private LibraryService service;
	
	@Autowired
	private MapStructMapper mapper;
	
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
		bookRepository.deleteAll();
		bookRepository.flush();
		authorRepository.deleteAll();
		authorRepository.flush();
	}
	
	@Test
	void testServiceSelAllBooksByRepository() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		List<Book> allRepository = bookRepository.findAll();
		List<Book> allService = service.selAllBooks()
				.stream()
				.map(mapper :: bookDtoToBook)
				.collect(Collectors.toList());
		assertEquals(allRepository, allService);
	}
	
	@Test
	void testServiceSelBookByRepository() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		Book bookByRepository = bookRepository.findById(book.getIsbn()).orElse(null);
		Book bookByService = mapper.bookDtoToBook(service.selBookByIsbn(book.getIsbn()));
		assertEquals(bookByRepository, bookByService);
	}
	
	@Test
	void testServiceDeleteBook() {
			Book bookToDelete = new Book("978-88-111-1111-1", "Title", "Crime", "Plot",
					LocalDate.of(2000, 01, 01), 100, Set.of());
			Author author = new Author(null, "FirstName", "LastName", Set.of(bookToDelete));
			bookToDelete.setAuthors(Set.of(author));
			bookRepository.save(bookToDelete);
			assertTrue(bookRepository.findById(bookToDelete.getIsbn()).isPresent());
			service.delBook("978-88-111-1111-1");	
			assertFalse(bookRepository.findById(bookToDelete.getIsbn()).isPresent());
		}
	
	@Test
	void testServiceCanInsertBookToRepository() {
		Book saved = service.insertNewBook(
				new BookDto("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
						Set.of(new AuthorSlimDto(null, "FistName", "LastName")))
				);
		
		assertTrue(bookRepository.findById(saved.getIsbn()).isPresent());
	}
	
	@Test
	void testServiceCanUpdateBookToRepository() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		BookDto update = new BookDto("978-88-111-1111-1", "New Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(new AuthorSlimDto(1L, "FirstName", "LastName")));
		service.saveBook(update);

		assertThat(bookRepository.findById(book.getIsbn())).contains(mapper.bookDtoToBook(update));
	}

	@Test
	void testServiceFindBooksByGenre() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		List<Book> result = service.findBooksByGenre("Crime")
				.stream()
				.map(mapper :: bookDtoToBook)
				.collect(Collectors.toList());
		assertEquals(bookRepository.findByGenreLike("Crime"), result);
	}
	
	@Test
	void testServiceFindBooksByPublicationDate() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2001, 02, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		List<Book> result = service.findBooksAfterPublicationDate(LocalDate.of(2000, 01, 01))
				.stream()
				.map(mapper :: bookDtoToBook)
				.collect(Collectors.toList());
		assertEquals(bookRepository.findByAfterPublicationDate(LocalDate.of(2000, 01, 01)), result);
	}
	
	@Test
	void testServiceFindBooksByTitle() {
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				null);
		Author author = new Author(1L, "FirstName", "LastName", Set.of(book));
		book.setAuthors(Set.of(author));
		bookRepository.save(book);
		List<Book> result = service.findBooksTitleContains("Title")
				.stream()
				.map(mapper :: bookDtoToBook)
				.collect(Collectors.toList());
		assertEquals(bookRepository.findByTitleContains("Title"), result);
	}
	
	@Test
	void testSeviceSelAllAuthorsByRepository() {
		Author author = new Author(null, "FirstName", "LastName", null);
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(author));
		author.setBooks(Set.of(book));
		bookRepository.save(book);
		List<Author> allByRepository = authorRepository.findAll();
		List<AuthorDto> allByService = service.selAllAuthors();
		
		assertEquals(allByRepository.size(), allByService.size(), 1);
		assertEquals(allByRepository.get(0).getId(), allByService.get(0).getId());
	}
	
	@Test
	void findAuthorById() {
		Author author = new Author(null, "FirstName", "LastName", null);
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(author));
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		Author authorByRepository = authorRepository.findById(author.getId()).orElse(null); 
		AuthorDto authorByService = service.selAuthorById(author.getId());
		assertEquals(authorByRepository.getId(), authorByService.getId());
		assertEquals(authorByRepository.getFirstName(), authorByService.getFirstName());
	}
	
	@Test
	void saveAuthorByService() {
		Author author = new Author(null, "FirstName", "LastName", null);
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(author));
		author.setBooks(Set.of(book));
		authorRepository.save(author);
		AuthorSlimDto update = new AuthorSlimDto(author.getId(), "NewName", "LastName");
		service.saveAuthor(update);
		assertThat(authorRepository.findById(author.getId())).contains(mapper.authorSlimDtoToAuthor(update));
	}
	
	@Test 
	void findAuthorByName() {
		Author author = new Author(1L, "FirstName", "LastName", null);
		Book book = new Book("978-88-111-1111-1", "Title", "Crime", "Plot", LocalDate.of(2000, 01, 01), 100, 
				Set.of(author));
		author.setBooks(Set.of(book));
		bookRepository.save(book);
		Author byRepository = authorRepository.findByNameLike("FirstName", "LastName");	
		AuthorDto byService = service.findAuthorByNameAndSurname("FirstName", "LastName");
		
		assertEquals(byRepository.getFirstName(), byService.getFirstName());
		assertEquals(byRepository.getLastName(), byService.getLastName());
	}
	
}
