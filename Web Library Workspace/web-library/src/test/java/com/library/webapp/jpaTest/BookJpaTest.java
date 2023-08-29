package com.library.webapp.jpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class BookJpaTest {
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	void testjpaMapping() {
		Book saved = entityManager.persistAndFlush(new Book("978-88-000-0000-1", "title"));
		
		assertThat(saved.getIsbn()).isEqualTo("978-88-000-0000-1");
		assertThat(saved.getTitle()).isEqualTo("title");
		assertThat(saved.getNumberPages()).isNotNull();
		assertThat(saved.getNumberPages()).isEqualTo(0);
	}
	
	@Test
	void test2BookNotEqual(){
		Book book1 = new Book("978-88-000-0000-1", "title");
		Book book2 = new Book("978-88-000-0000-2", "title");
		
		assertFalse(book1.equals(book2));
	}
	
	@Test
	void testFind2BookWithDifferentRecords() {
		String isbnBook1 = "978-88-000-0000-1";
		String isbnBook2 =  "978-88-000-0000-2";
		entityManager.persistAndFlush(new Book(isbnBook1, "title"));
		entityManager.persistAndFlush(new Book(isbnBook2, "title"));
		Book result1 = entityManager.find(Book.class, isbnBook1);
		Book result2 = entityManager.find(Book.class, isbnBook2);
		
		assertFalse(result1.equals(result2));
		}
	
	
	@Test
	void test2BookWithSameRecord_OneBookPersist() {
		String isbnBook = "978-88-000-0000-1";
		entityManager.persistAndFlush(new Book(isbnBook, "title"));
		Book book = new Book(isbnBook, "titleBook"); 
		Book result = entityManager.find(Book.class, isbnBook);
		
		assertTrue(book.equals(result));
	}
	
	@Test
	void test2BookWithSameRecord_AllBookPersist() {
		String isbnBook = "978-88-000-0000-1";
		entityManager.persistAndFlush(new Book(isbnBook, "title"));
		Book book = new Book(isbnBook, "titleBook"); 
		entityManager.merge(book);
		Book result = entityManager.find(Book.class, isbnBook);
		
		assertTrue(book.equals(result));
	}
}
