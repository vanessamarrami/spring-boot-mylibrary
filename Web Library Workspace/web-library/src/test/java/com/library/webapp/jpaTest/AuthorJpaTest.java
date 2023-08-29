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
import com.library.webapp.entity.Author;

@DataJpaTest
@ContextConfiguration(
		  classes = { H2Config.class }, 
		  loader = AnnotationConfigContextLoader.class)
class AuthorJpaTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	void testjpaMapping() {
		Author saved = entityManager.persistAndFlush(new Author(null ,"firstName", "lastName", null));
		
		assertThat(saved.getFirstName()).isEqualTo("firstName");
		assertThat(saved.getLastName()).isEqualTo("lastName");
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getId()).isPositive();	
	}
	
	@Test
	void testAutoincrementID() {
		Author author1 = entityManager.persistAndFlush(new Author(null ,"firstName1", "lastName1", null));
		Author author2 = entityManager.persistAndFlush(new Author(null ,"firstName2", "lastName2", null));
		
		assertThat(author1.getId()).isNotNull();
		assertThat(author2.getId()).isNotNull();
		assertThat(author2.getId()).isGreaterThan(author1.getId());	
	}

	@Test
	void test2AuthorsNotEqual(){
		Author author1 = new Author(null, "Agatha", "Christie", null);
		Author author2 = new Author(null, "George", "Orwell", null);
		assertFalse(author1.equals(author2));
	}
	
	@Test
	void test2AuthorsNotEqual_persist() {
		Author result1 = entityManager.persistAndFlush(new Author(null, "Agatha", "Christie", null));
		Author result2 = entityManager.persistAndFlush(new Author(null, "George", "Orwell", null));
		assertFalse(result1.equals(result2));
	}
	
	@Test
	void test2AuthorsEqual_OneAuthorPersist() {
		Author result = entityManager.persistAndFlush(new Author(null, "Agatha", "Christie", null));
		Author author = new Author(null, "Agatha", "Christie", null);
		assertTrue(result.equals(author));
	} 
	
	@Test
	void test2AuthorsEqual_AllPersist() {
		Author result1 = entityManager.persistAndFlush(new Author(null, "Agatha", "Christie", null));
		Author result2 = entityManager.persistAndFlush(new Author(null, "Agatha", "Christie", null));
		assertTrue(result1.equals(result2));
	}
	
}
