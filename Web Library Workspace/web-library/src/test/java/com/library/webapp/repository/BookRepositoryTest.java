package com.library.webapp.repository;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.library.webapp.config.H2Config;
import com.library.webapp.entity.Book;

@DataJpaTest
@ContextConfiguration(
		  classes = { H2Config.class }, 
		  loader = AnnotationConfigContextLoader.class)
class BookRepositoryTest {

	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	private TestEntityManager entityManager;
	 
	@Test
	void testGelAllBook() {
		Book book1 = new Book ("978-88-000-0000-1", "First");		
		entityManager.persist(book1);
		Book book2 = new Book("978-88-000-0000-2", "Second");
		entityManager.persist(book2);
		Collection<Book> books = bookRepository.findAll();
		
		assertThat(books).containsExactly(book1, book2);
		assertEquals(2, books.size());
	}
	
	@Test
	void findBookByGenre() {
		Book book = new Book("978-88-000-0000-1", "titleBook", "GenreBook", null, null, 0, null);
		entityManager.persistAndFlush(book);
		List<Book> result = bookRepository.findByGenreLike("GenreBook");
		
		assertThat(result).contains(book);
	} 
	
	@Test
	void findBookByGenre_isEmpty() {
		List<Book> result = bookRepository.findByGenreLike("GenreBook");
		assertThat(result).isEmpty();
	}
	
	@Test
	void findBookAfterPublicationDate() {
		Book bookAfter = new Book("978-88-000-0000-1", "BookAfter", "GenreBook", null, LocalDate.parse("2000-06-23"), 0, null);
		entityManager.persist(bookAfter);
		Book bookBefore = new Book("978-88-000-0000-2", "BookBefore", "GenreBook", null,LocalDate.parse("1997-01-10"), 0, null);
		entityManager.persist(bookBefore);
		Book bookBefore2 = new Book("978-88-000-0000-3", "BookAfter2", "GenreBook", null, LocalDate.parse("1990-01-02"), 0, null);
		entityManager.persist(bookBefore2);
		
		List<Book> result = bookRepository.findByAfterPublicationDate(LocalDate.parse("2000-01-01"));
		assertThat(result).contains(bookAfter);
	}
	
	@Test
	void findBookAfterPublicationDate_isEmpty() {
		List<Book> result = bookRepository.findByAfterPublicationDate(LocalDate.parse("2000-01-01"));
		assertThat(result).isEmpty();
	}
	
	
	@Test
	void findBookTitleContains() {
		Book bookFound1 = new Book("978-88-000-0000-1", "TitleBookOk", "GenreBook", null,  LocalDate.parse("2000-02-01"), 0, null);
		entityManager.persist(bookFound1);
		Book bookFound2 = new Book("978-88-000-0000-2", "AnotherTitleForTheBook", "GenreBook", null,  LocalDate.parse("2000-01-01"), 0, null);
		entityManager.persist(bookFound2);
		Book notFound = new Book("978-88-000-0000-3", "TitleNoOk", "GenreBook", null,  LocalDate.parse("2000-03-01"), 0, null);
		entityManager.persist(notFound);
		List<Book> result = bookRepository.findByTitleContains("Book");
		
		assertThat(result).containsExactly(bookFound2, bookFound1);	
	}
	
	@Test
	void findBookTitleContains_isEmpty() {
		List<Book> result = bookRepository.findByTitleContains("title");
		assertThat(result).isEmpty();
	}

}
