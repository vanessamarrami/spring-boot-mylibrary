package com.library.webapp.dtos.mapper;

import org.mapstruct.Mapper;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.dtos.BookSlimDto;
import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;

@Mapper
public interface MapStructMapper {
	
	 BookSlimDto bookToBookSlimDto(Book book); 
	 Book bookDtoToBook(BookDto  bookDto);
	 BookDto bookToBookDto(Book book); 
	 AuthorDto  authorToAuthorDto(Author author); 
	 AuthorSlimDto authorToAuthorSlimDto(Author author); 
	 Author authorSlimDtoToAuthor(AuthorSlimDto authorPostDto);
	 
}
