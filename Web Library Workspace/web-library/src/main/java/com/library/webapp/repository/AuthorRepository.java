package com.library.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.library.webapp.entity.Author;

@Repository
@Transactional(readOnly = true)
public interface AuthorRepository extends JpaRepository<Author, Long> {
	
	@Query(value = "SELECT * FROM author a WHERE (first_name = :firstName and last_name = :lastName)", nativeQuery = true )
	Author findByNameLike(String firstName, String lastName);
	
}
