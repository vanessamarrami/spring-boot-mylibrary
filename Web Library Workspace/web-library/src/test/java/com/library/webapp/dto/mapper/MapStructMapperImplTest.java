package com.library.webapp.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.dtos.BookSlimDto;
import com.library.webapp.dtos.mapper.MapStructMapperImpl;
import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;

@ExtendWith(MockitoExtension.class) 
class MapStructMapperImplTest {

	@InjectMocks
	private MapStructMapperImpl mapper;
	
	@Test
	void testMappingBookToBookDto() {
		Author authorToMapSlim = new Author(1L, "firstName", "lastName", null);
		AuthorSlimDto authorSlimDtoResult = new AuthorSlimDto(1L, "firstName", "lastName");
		Book bookToMap = new Book("978-88-000-0000-1", "title", "genre", "plot", LocalDate.parse("2000-01-01"), 100, Set.of(authorToMapSlim));
		BookDto result = mapper.bookToBookDto(bookToMap);
		
		assertEquals(result.getIsbn(), bookToMap.getIsbn());
		assertEquals(result.getTitle(), bookToMap.getTitle());
		assertEquals(result.getGenre(), bookToMap.getGenre());
		assertEquals(result.getPlot(), bookToMap.getPlot());
		assertEquals(result.getPublicationDate(), bookToMap.getPublicationDate());
		assertEquals(result.getNumberPages(), bookToMap.getNumberPages());
		assertThat(result.getAuthors()).contains(authorSlimDtoResult);
	}

	@Test
	void testMappingBookDtoToBook(){
		AuthorSlimDto authorSlimToMap = new AuthorSlimDto(1L, "firstName", "lastName");
		Author authorResult = new Author(1L, "firstName", "lastName", null);
		BookDto bookDtoToMap = new BookDto("978-88-000-0000-1", "title", "genre", "plot", LocalDate.parse("2000-01-01"), 100, Set.of(authorSlimToMap));
		Book result = mapper.bookDtoToBook(bookDtoToMap);
		authorResult.setBooks(Set.of(result));
		
		assertEquals(result.getIsbn(), bookDtoToMap.getIsbn());
		assertEquals(result.getTitle(), bookDtoToMap.getTitle());
		assertEquals(result.getGenre(), bookDtoToMap.getGenre());
		assertEquals(result.getPlot(), bookDtoToMap.getPlot());
		assertEquals(result.getPublicationDate(), bookDtoToMap.getPublicationDate());
		assertEquals(result.getNumberPages(), bookDtoToMap.getNumberPages());
		assertThat(result.getAuthors()).contains(authorResult);
	}
	
	@Test
	void testMappingBookToBookDto_Null(){
		assertThat(mapper.bookToBookDto(null)).isNull();
	}

	@Test
	void testMappingAuthorToAuthorDto(){
		Book bookToMapSlim = new Book("978-88-000-0000-1", "title", "genre", "plot", 
				LocalDate.parse("2000-01-01"), 100, null);
		BookSlimDto bookSlimDtoResult = new BookSlimDto("978-88-000-0000-1", "title");
		Author authorToMap = new Author(1L, "firstName", "lastName", Set.of(bookToMapSlim));
		AuthorDto result = mapper.authorToAuthorDto(authorToMap);
		
		assertEquals(result.getId(), authorToMap.getId());
		assertEquals(result.getFirstName(), authorToMap.getFirstName());
		assertEquals(result.getLastName(), authorToMap.getLastName());
		assertThat(result.getBooks()).contains(bookSlimDtoResult);
	}
	
	@Test
	void testMappingAuthorToAuthorDto_null(){
		assertThat(mapper.authorToAuthorDto(null)).isNull();
	}
	
	@Test
	void testMappingAuthorSlimDtoToAuthor() {
		AuthorSlimDto authorSlimToMap = new AuthorSlimDto(1L, "firstName", "lastName");
		Author result = mapper.authorSlimDtoToAuthor(authorSlimToMap);
		
		assertEquals(result.getId(), authorSlimToMap.getId());
		assertEquals(result.getFirstName(), authorSlimToMap.getFirstName());
		assertEquals(result.getLastName(), authorSlimToMap.getLastName());
	}
}
