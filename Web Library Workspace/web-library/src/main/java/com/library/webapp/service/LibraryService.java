package com.library.webapp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.entity.Book;

public interface LibraryService {
	List<BookDto> selAllBooks();
	BookDto selBookByIsbn(String isbn);
	boolean delBook(String isbn);
	Book insertNewBook(BookDto newBook);
	boolean saveBook(BookDto book);
	List<BookDto> findBooksByGenre(String genre);
	List<BookDto> findBooksAfterPublicationDate(LocalDate date);
	List<BookDto> findBooksTitleContains(String title);
	
	List<AuthorDto> selAllAuthors();
	AuthorDto selAuthorById(Long id);
	boolean saveAuthor(AuthorSlimDto authorToSave);
	AuthorDto findAuthorByNameAndSurname(String firstName, String lastName);
	

	Set<AuthorSlimDto> fillAuthorsSetByStringName(String authorNames);
	String fillStringNameBySet(Set<AuthorSlimDto> authors);
}
