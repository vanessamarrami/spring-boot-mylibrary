package com.library.webapp.dtos.mapper;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.dtos.BookSlimDto;
import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;

@Component
public class MapStructMapperImpl implements MapStructMapper{

	@Override 
	public BookSlimDto bookToBookSlimDto(Book book) {
		BookSlimDto bookSlimDto = new BookSlimDto();
		bookSlimDto.setIsbn(book.getIsbn());
		bookSlimDto.setTitle(book.getTitle());
		return bookSlimDto;
	}
	
	@Override 
	public BookDto bookToBookDto(Book book) {
		if(book == null) return null; 
		BookDto bookDto = new BookDto();
		bookDto.setIsbn(book.getIsbn());
		bookDto.setTitle(book.getTitle());
		bookDto.setGenre(book.getGenre());
		bookDto.setPlot(book.getPlot());
		bookDto.setPublicationDate(book.getPublicationDate());
		bookDto.setNumberPages(book.getNumberPages());
		bookDto.setAuthors(authorToAuthorSlimDtoSet(book.getAuthors())); 
		return bookDto;
	}
	
	
	@Override
	public Book bookDtoToBook(BookDto bookDto) {
		Book book = new Book();
		book.setIsbn(bookDto.getIsbn());
		book.setTitle(bookDto.getTitle());
		book.setGenre(bookDto.getGenre());
		book.setPlot(bookDto.getPlot());
		book.setPublicationDate(bookDto.getPublicationDate());
		book.setNumberPages(bookDto.getNumberPages());
		book.addAuthors(authorSlimDtoToAuthorSet(bookDto.getAuthors())); 
		return book;
	}
	
	@Override 
	public AuthorDto authorToAuthorDto(Author author) { 
		if(author == null) return null;
		AuthorDto authorDto = new AuthorDto();
		authorDto.setId(author.getId());
		authorDto.setFirstName(author.getFirstName());
		authorDto.setLastName(author.getLastName());
		authorDto.setBooks(bookToBookSlimDtoSet(author.getBooks()));
		return authorDto;
	}

	@Override 
	public AuthorSlimDto authorToAuthorSlimDto(Author author) {
		AuthorSlimDto authorSlimDto = new AuthorSlimDto();
		authorSlimDto.setId(author.getId());
		authorSlimDto.setFirstName(author.getFirstName());
		authorSlimDto.setLastName(author.getLastName());
		return authorSlimDto;
	}
	
	@Override
	public Author authorSlimDtoToAuthor(AuthorSlimDto authorSlimDto) {
		Author author = new Author();
		author.setId(authorSlimDto.getId());
		author.setFirstName(authorSlimDto.getFirstName());
		author.setLastName(authorSlimDto.getLastName());
		return author;
	}
	
	Set<Author> authorSlimDtoToAuthorSet(Set<AuthorSlimDto> authorsSlimDto){
		Set<Author> authors = new HashSet<Author>(authorsSlimDto.size());
		for (AuthorSlimDto authorSlim : authorsSlimDto) {
			authors.add(authorSlimDtoToAuthor(authorSlim));
		}
		return authors; 
	}
	

	Set<AuthorSlimDto> 	authorToAuthorSlimDtoSet(Set<Author> authors){
		Set<AuthorSlimDto> authorSlimDto = new HashSet<AuthorSlimDto>(authors.size());
		for (Author author : authors) {
			authorSlimDto.add(authorToAuthorSlimDto(author));
		}
		return authorSlimDto;
	}
	
	
	Set<BookSlimDto> bookToBookSlimDtoSet(Set<Book> books){
		Set<BookSlimDto> bookSlimDto = new HashSet<BookSlimDto>(books.size());
		for (Book book : books) {
			bookSlimDto.add(bookToBookSlimDto(book));
		}
		return bookSlimDto;
	}
	
}

