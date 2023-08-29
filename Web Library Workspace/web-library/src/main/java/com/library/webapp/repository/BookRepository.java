package com.library.webapp.repository;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.library.webapp.entity.Book;

@Repository
@Transactional(readOnly = true)
public interface BookRepository extends JpaRepository<Book, String> {
	
	@Query(value = "SELECT * FROM book WHERE genre = ? ", nativeQuery = true)
	List<Book>  findByGenreLike(String genre);
	
	@Query(value = "SELECT * FROM book b WHERE b.publication_date > :date ORDER BY PUBLICATION_DATE", nativeQuery = true)
	List<Book> findByAfterPublicationDate(LocalDate date);
	
	
	@Query(value = "SELECT * FROM book b WHERE TITLE LIKE CONCAT('%', :title, '%') ORDER BY PUBLICATION_DATE", nativeQuery = true)
	List<Book> findByTitleContains(String title);

}
