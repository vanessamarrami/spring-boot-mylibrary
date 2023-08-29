package com.library.webapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.library.webapp.dtos.AuthorDto;
import com.library.webapp.dtos.AuthorSlimDto;
import com.library.webapp.dtos.BookDto;
import com.library.webapp.dtos.mapper.MapStructMapper;
import com.library.webapp.entity.Author;
import com.library.webapp.entity.Book;
import com.library.webapp.exceptions.DuplicateException;
import com.library.webapp.exceptions.NotFoundException;
import com.library.webapp.repository.AuthorRepository;
import com.library.webapp.repository.BookRepository;


@ExtendWith(MockitoExtension.class) 
class LibraryServiceImplTest {

	@InjectMocks
	private LibraryServiceImpl libraryService;
	
	@Mock
	private MapStructMapper mapper;
	
	@Mock
	private BookRepository bookRepository;
	
	@Mock
	private AuthorRepository authorRepository;
	
	@Test
	void testSelAllBooks() {
		Book book1 = new Book("978-88-000-0000-1", "Book1");
		Book book2 = new Book("978-88-000-0000-2", "Book2");
		when(bookRepository.findAll()).thenReturn(List.of(book1, book2));
		
		BookDto bookDto1 = new BookDto("978-88-000-0000-1", "Book1", null, null, null, 0, null);
		BookDto bookDto2 = new BookDto("978-88-000-0000-2", "Book2", null, null, null, 0, null);
		when(mapper.bookToBookDto(book1)).thenReturn(bookDto1);
		when(mapper.bookToBookDto(book2)).thenReturn(bookDto2);
		
		List<BookDto> result = libraryService.selAllBooks();
		assertThat(result).containsExactly(bookDto1, bookDto2);
	}
	
	@Test
	void testSelAllBook_NoFound() throws Exception {
		when(bookRepository.findAll()).thenReturn(List.of());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.selAllBooks();
	        });
		
	    assertEquals("There is no book!", exception.getMessage());
	}
	
	@Test
	void testSelBookByIsbn() {
		Book book = new Book("978-88-000-0000-1", "Book");
		when(bookRepository.findById("978-88-000-0000-1")).thenReturn(Optional.of(book));
		BookDto bookDto = new BookDto("978-88-000-0000-1", "Book", null, null, null, 0, null);
		when(mapper.bookToBookDto(book)).thenReturn(bookDto);
		BookDto result = libraryService.selBookByIsbn("978-88-000-0000-1");
		
		assertEquals(result, bookDto);
	}
	
	@Test
	void testSelBookById_NotFound() {
		when(bookRepository.findById(anyString())).thenReturn(Optional.empty());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.selBookByIsbn("978-88-000-0000-1");
	        });
		
	    assertEquals("Book not present or wrong ISBN!", exception.getMessage());
	}
	
	@Test
	void testDelBook() {
		Author author = new Author(1L, "firstName", "lastName", null);
		Set<Author> authors = new HashSet<Author>();
		Set<Author> authorsToDelete = new HashSet<Author>();
		authorsToDelete.add(author);
		authors.add(author);
		Book book = new Book("978-88-000-0000-1", "titleBook", "GenreBook", "plot", LocalDate.parse("2000-06-23"), 0, authors);
		
		Set<Book> books = new HashSet<Book>();
		books.add(book);
		author.setBooks(books);
		
		when(bookRepository.findById("978-88-000-0000-1")).thenReturn(Optional.of(book));
		doNothing().when(bookRepository).deleteById("978-88-000-0000-1");
		doNothing().when(authorRepository).deleteAll(authorsToDelete);
		libraryService.delBook("978-88-000-0000-1");
		
		verify(bookRepository).deleteById(book.getIsbn());
		verify(authorRepository).deleteAll(authorsToDelete);
		assertTrue(true);
	}
	
	@Test
	void testDelBook_withAuthorAnotherBook() {
		Author author = new Author(1L, "firstName", "lastName", null);
		Set<Author> authors = new HashSet<Author>();
		Set<Author> authorsToDelete = new HashSet<Author>();
		authors.add(author);
		Book book = new Book("978-88-000-0000-1", "titleBook", "GenreBook", "plot", LocalDate.parse("2000-06-23"), 0, authors);
		
		Set<Book> books = new HashSet<Book>();
		books.add(book);
		books.add(new Book("978-88-000-0000-2", "title1"));
		author.setBooks(books);
		
		when(bookRepository.findById("978-88-000-0000-1")).thenReturn(Optional.of(book));
		doNothing().when(bookRepository).deleteById("978-88-000-0000-1");
		doNothing().when(authorRepository).deleteAll(authorsToDelete);
		libraryService.delBook("978-88-000-0000-1");
		
		verify(bookRepository).deleteById(book.getIsbn());
		verify(authorRepository).deleteAll(authorsToDelete);
		assertTrue(true);
	}

	@Test
	void testDelBook_Exceptions() {
		when(bookRepository.findById(anyString())).thenReturn(Optional.empty());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.delBook("978-88-000-0000-1");
	        });
		
	    assertEquals("It is not possible to delete the book with ISBN 978-88-000-0000-1 because it is not present!", exception.getMessage());
	}
	
	@Test
	void testInsertNewBook() {
		Author author = new Author(null, "firstName", "lastName", null);
		Book newBook = spy(new Book("978-88-000-0000-1", "titleBook", null, null, null, 0, Set.of(author)));
		AuthorSlimDto authorSlim = new AuthorSlimDto(null, "fistName", "lastName");
		BookDto newBookDto = new BookDto("978-88-000-0000-1", "titleBook", null, null, null, 0, Set.of(authorSlim));
		
		when(bookRepository.findById(anyString())).thenReturn(Optional.empty());
		when(mapper.bookDtoToBook(newBookDto)).thenReturn(newBook);
		when(authorRepository.findByNameLike("firstName", "lastName")).thenReturn(null);
		when(bookRepository.save(any(Book.class))).thenReturn(newBook);
		Book result = libraryService.insertNewBook(newBookDto);
	 
		assertThat(result).isSameAs(newBook);
		InOrder inOrder = Mockito.inOrder(newBook, bookRepository);
		inOrder.verify(bookRepository).save(newBook);
	}
	
	@Test
	void testInsertNewBook_existingAuthor() {
		Author author = new Author(null, "firstName", "lastName", null);
		Book newBook = spy(new Book("978-88-000-0000-1", "titleBook", null, null, null, 0, Set.of(author)));
		AuthorSlimDto authorSlim = new AuthorSlimDto(null, "fistName", "lastName");
		BookDto newBookDto = new BookDto("978-88-000-0000-1", "titleBook", null, null, null, 0, Set.of(authorSlim));
		
		when(bookRepository.findById(anyString())).thenReturn(Optional.empty());
		when(mapper.bookDtoToBook(newBookDto)).thenReturn(newBook);
		when(authorRepository.findByNameLike("firstName", "lastName"))
		.thenReturn(
				new Author(1L, "firstName", "lastName", null));
		when(bookRepository.save(any(Book.class))).thenReturn(newBook);
		Book result = libraryService.insertNewBook(newBookDto);
	 
		assertThat(result).isSameAs(newBook);
		assertEquals(1L, author.getId());
		InOrder inOrder = Mockito.inOrder(newBook, bookRepository);
		inOrder.verify(bookRepository).save(newBook);
	}
	
	@Test
	void testInsertNewBook_Exceptions() {
		Book book = new Book("978-88-000-0000-1", "title");
		when(bookRepository.findById("978-88-000-0000-1")).thenReturn(Optional.of(book));
		DuplicateException exception = assertThrows(DuplicateException.class, () -> {
			libraryService.insertNewBook(new BookDto("978-88-000-0000-1", "title", "genre", "plot", null, 0, null));
	        });
		
	    assertEquals("The book with ISBN 978-88-000-0000-1 is already there!", exception.getMessage());
	}
	
	@Test
	void testSaveBook() {
		BookDto bookToUpdate = spy(new BookDto("978-88-000-0000-1", "titleBook", null, null, null, 0, null));
		bookToUpdate.setTitle("newTitleBook");
		Book updated = new Book("978-88-000-0000-1", "newTitleBook");	
		
		when(bookRepository.findById(anyString())).thenReturn(Optional.of(updated));
		when(mapper.bookDtoToBook(bookToUpdate)).thenReturn(updated);
		when(bookRepository.save(any(Book.class))).thenReturn(updated);	
		libraryService.saveBook(bookToUpdate);
	    
	    assertThat(bookToUpdate.getTitle()).isSameAs(updated.getTitle());
	    assertTrue(true);
	}
	
	@Test
	void testSaveBook_Exceptions() {
		when(bookRepository.findById(anyString())).thenReturn(Optional.empty());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.saveBook(new BookDto("978-88-000-0000-1", "title", "genre", "plot", null, 0, null));
	        });
		
	    assertEquals("It is not possible to edit the book with ISBN 978-88-000-0000-1 because it is not present!", exception.getMessage());
	}
	
	@Test
	void testFindBooksByGenre() {
		Book book = new Book("978-88-000-0000-1", "titleBook", "GenreBook", null, null, 0, null);
		BookDto bookDto = new BookDto("978-88-000-0000-1", "titleBook", "GenreBook", null, null, 0, null);
		when(bookRepository.findByGenreLike("GenreBook")).thenReturn(List.of(book));
		when(mapper.bookToBookDto(book)).thenReturn(bookDto);
		List<BookDto> books = libraryService.findBooksByGenre("GenreBook");
		
		assertThat(books).containsExactly(bookDto);
	}
	
	@Test
	void testSelBooksByGenre_NotFound() throws Exception {
		when(bookRepository.findByGenreLike(anyString())).thenReturn(List.of());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.findBooksByGenre("genreBook");
	        });
		
	    assertEquals("There are no books with genre genreBook!", exception.getMessage());
	}
	
	@Test
	void findBooksAfterPublicationDate() {
		Book bookAfter = new Book("978-88-000-0000-1", "titleBook", "GenreBook", null, LocalDate.parse("2000-06-23"), 0, null);
		BookDto bookAfterDto = new BookDto("978-88-000-0000-1", "titleBook", "GenreBook", null,  LocalDate.parse("2000-06-23"), 0, null);
		when(bookRepository.findByAfterPublicationDate(LocalDate.parse("2000-01-01"))).thenReturn(List.of(bookAfter));
		when(mapper.bookToBookDto(bookAfter)).thenReturn(bookAfterDto);
		List<BookDto> books = libraryService.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01"));
		
		assertThat(books).containsExactly(bookAfterDto);
	}
	
	@Test
	void testSelBooksAfterPublicationDate_NotFound() throws Exception {
		when(bookRepository.findByAfterPublicationDate(LocalDate.parse("2000-01-01"))).thenReturn(List.of());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.findBooksAfterPublicationDate(LocalDate.parse("2000-01-01"));
	        });
		
	    assertEquals("There are no books published after 2000-01-01!", exception.getMessage());
	}
	
	@Test
	void findBooksTitleContains() {
		Book book = new Book("978-88-000-0000-1" , "titleABCBook");
		BookDto bookDto = new BookDto("978-88-000-0000-1" , "titleABCBook", null, null, null, 0, null);
		when(bookRepository.findByTitleContains("ABC")).thenReturn(List.of(book));
		when(mapper.bookToBookDto(book)).thenReturn(bookDto);
		List<BookDto> result = libraryService.findBooksTitleContains("ABC");
		
		assertThat(result).contains(bookDto);
	}
	
	@Test
	void findBooksALLTitle() {
		Book book = new Book("978-88-000-0000-1" , "titleABCBook");
		BookDto bookDto = new BookDto("978-88-000-0000-1" , "titleABCBook", null, null, null, 0, null);
		when(bookRepository.findByTitleContains("titleABCBook")).thenReturn(List.of(book));
		when(mapper.bookToBookDto(book)).thenReturn(bookDto);
		List<BookDto> result = libraryService.findBooksTitleContains("titleABCBook");
		
		assertThat(result).contains(bookDto);
	}
	
	@Test
	void testSelBooksTitleContains_NotFound() throws Exception {
		when(bookRepository.findByTitleContains(anyString())).thenReturn(List.of());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.findBooksTitleContains("titoloProva");
	        });
		
	    assertEquals("There are no books with titoloProva in the title!", exception.getMessage());
	}
	
	@Test
	void testSelAllAuthors(){
		Author author1 = new Author(1L, "firstName1", "lastName1", null);
		Author author2 = new Author(2L, "firstName2", "lastName2", null);
		when(authorRepository.findAll()).thenReturn(List.of(author1, author2));
		
		AuthorDto authorDto1 = new AuthorDto(1L, "firstName1", "lastName1", null);
		AuthorDto authorDto2 = new AuthorDto(2L, "firstName2", "lastName2", null);		
		when(mapper.authorToAuthorDto(author1)).thenReturn(authorDto1);
		when(mapper.authorToAuthorDto(author2)).thenReturn(authorDto2);
		
		List<AuthorDto> result = libraryService.selAllAuthors();
		assertThat(result).contains(authorDto1, authorDto2);		
	}
	
	@Test
	void testSelAllAuthors_NotFound() throws Exception {
		when(authorRepository.findAll()).thenReturn(List.of());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.selAllAuthors();
	        });
		
	    assertEquals("There is no author!", exception.getMessage());
	}
	
	@Test
	void testGetAuthorById() {
		Author author = new Author(1L, "firstName", "lastName", null);
		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		AuthorDto authorDto = new AuthorDto(1L, "firstName", "lastName", null);
		when(mapper.authorToAuthorDto(author)).thenReturn(authorDto);
		AuthorDto result = libraryService.selAuthorById(1L);
		
		assertThat(result).isSameAs(authorDto);
	}
	
	@Test
	void testSelAuthorById_notFound() {
		when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.selAuthorById(1L);
	        });
		
	    assertEquals("Author not present or wrong ID!", exception.getMessage());
	}
	
	@Test
	void testSaveAuthor() {
		AuthorSlimDto authorToUpdate = spy(new AuthorSlimDto(1L, "authorOldFirstName", "lastName"));
		authorToUpdate.setFirstName("authorNewFirstName");
		Author updated = new Author(1L, "authorNewFirstName", "lastName", null);	
		when(authorRepository.findById(1L)).thenReturn(Optional.of(updated));
		when(authorRepository.save(any(Author.class))).thenReturn(updated);	
		when(mapper.authorSlimDtoToAuthor(authorToUpdate)).thenReturn(updated);
		libraryService.saveAuthor(authorToUpdate);
	    
	    assertEquals(authorToUpdate.getFirstName(), updated.getFirstName());
	    assertTrue(true);
	}
	
	@Test
	void testSaveAuthor_Exceptions() {
		when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.saveAuthor(new AuthorSlimDto(1L, "firstName", "lastName"));
	        });
		
	    assertEquals("It is not possible to modify the author with ID 1 because it is not present!", exception.getMessage());
	}
	
	@Test
	void testFindByNameAuthor() {
		Author author = new Author(1L, "firstName", "lastName", null);
		when(authorRepository.findByNameLike("firstName", "lastName")).thenReturn(author);
		AuthorDto authorDto = new AuthorDto(1L, "firstName", "lastName", null);
		when(mapper.authorToAuthorDto(author)).thenReturn(authorDto);
		AuthorDto result = libraryService.findAuthorByNameAndSurname("firstName", "lastName");	
		
		assertThat(result).isEqualTo(authorDto);
		assertEquals(author.getFirstName(), result.getFirstName());
		assertEquals(author.getLastName(), result.getLastName());
	}
	
	@Test
	void testFindByNameAuthor_Exceptions() {
		when(authorRepository.findByNameLike(anyString(), anyString())).thenReturn(null);	
		NotFoundException exception = assertThrows(NotFoundException.class, () -> {
			libraryService.findAuthorByNameAndSurname("firstName", "lastName");
	        });
		
	    assertEquals("There is no author with the specified name.", exception.getMessage());
	}
	
	@Test
	void testfillAuthorsSetByStringName() {
		String authorNames = "FirstNameA SecondNameA, FirstNameB SecondNameB";
		Set<AuthorSlimDto> authors = libraryService.fillAuthorsSetByStringName(authorNames);
		
		assertThat(authors).containsExactly(new AuthorSlimDto(null, "FirstNameA", "SecondNameA"), 
				new AuthorSlimDto(null, "FirstNameB", "SecondNameB"));
	}
	
	@Test
	void testfillStringNameBySet() {
		Set<AuthorSlimDto> authors = Set.of(
				new AuthorSlimDto(1L, "FirstNameA", "SecondNameA"), 
				new AuthorSlimDto(2L, "FirstNameB", "SecondNameB")
				);
		String authorNames = libraryService.fillStringNameBySet(authors);
		
		assertTrue(authorNames.equals("FirstNameA SecondNameA, FirstNameB SecondNameB") 
				|| authorNames.equals("FirstNameB SecondNameB, FirstNameA SecondNameA"));

	}

}
