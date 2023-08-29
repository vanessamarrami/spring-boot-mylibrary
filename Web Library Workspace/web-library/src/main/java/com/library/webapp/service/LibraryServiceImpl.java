package com.library.webapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional
public class LibraryServiceImpl implements LibraryService {

	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	AuthorRepository authorRepository;
	
	@Autowired
	private MapStructMapper mapstructMapper;
	
	@Override
	@Transactional(readOnly = true)
	public List<BookDto> selAllBooks() {
		List<Book> allBooks = bookRepository.findAll(); 
		if(allBooks.isEmpty()) throw new NotFoundException("There is no book!");
		List<BookDto> booksDto = new ArrayList<BookDto>();
		for (Book book : allBooks) {
			booksDto.add(mapstructMapper.bookToBookDto(book));
		}
		return booksDto; 
	}

	@Override
	@Transactional(readOnly = true)
	public BookDto selBookByIsbn(String isbn) {
		BookDto bookdto = mapstructMapper.bookToBookDto(bookRepository.findById(isbn).orElse(null));
		if(bookdto == null) {
			throw new NotFoundException("Book not present or wrong ISBN!");
		}
		return bookdto;
	}

	@Override
	public boolean delBook(String isbn) {
		Book book = bookRepository.findById(isbn).orElse(null);
		if(book == null){
			throw new NotFoundException(String.format("It is not possible to delete the book with ISBN %s because it is not present!", isbn));
		}
		
		Set<Author> authors = new HashSet<Author>();
		authors.addAll(book.getAuthors());
		book.removeAuthors(book.getAuthors());
		bookRepository.deleteById(isbn);
		Set<Author> authorsToDelete = new HashSet<Author>();
		for (Author author : authors) { // salvare in una collez -> delete all
			if(author.getBooks().isEmpty()) authorsToDelete.add(author);
		}
		authorRepository.deleteAll(authorsToDelete);
		return true;
	}

	@Override
	public Book insertNewBook(BookDto newBook) {
		if( (bookRepository.findById(newBook.getIsbn())).orElse(null) != null) {
			throw new DuplicateException(String.format("The book with ISBN %s is already there!", newBook.getIsbn()));
		}
		Book book = mapstructMapper.bookDtoToBook(newBook);
		associateWithExistingAuthor(book);
		bookRepository.save(book);
		return book;
	}

	@Override
	public boolean saveBook(BookDto bookDto) {
		Book bookFindById = bookRepository.findById(bookDto.getIsbn()).orElse(null);
		if(bookFindById == null){
			throw new NotFoundException(String.format("It is not possible to edit the book with ISBN %s because it is not present!", bookDto.getIsbn()));
		}
		Book book = mapstructMapper.bookDtoToBook(bookDto);
		/* inseriamo solo il nome dell'autore, quindi se esiste un autore con quel nome bisogna associarlo ad esso
		 * tramite l'id, altrimenti viene creato un nuovo autore con lo stesso nome. 
		 * Se non è presente l'autore con quel nome, viene creato. */
		associateWithExistingAuthor(book);
		bookRepository.save(book);
		return true;
	}

	private void associateWithExistingAuthor(Book book) {
		Set<Author> authors = book.getAuthors();
		for (Author author : authors) { 
			Author authorFound = authorRepository.findByNameLike(author.getFirstName(), author.getLastName());
			if(authorFound != null) {
				author.setId(authorFound.getId());
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<BookDto> findBooksByGenre(String genre) {
		List<Book> books = bookRepository.findByGenreLike(genre); 
		if(books.isEmpty()) throw new NotFoundException(String.format("There are no books with genre %s!", genre));
		List<BookDto> booksDto = new ArrayList<BookDto>();
		for (Book book : books) {
			booksDto.add(mapstructMapper.bookToBookDto(book));
		}
		return booksDto; 
	}

	@Override
	@Transactional(readOnly = true)
	public List<BookDto> findBooksAfterPublicationDate(LocalDate date) {
		List<Book> books = bookRepository.findByAfterPublicationDate(date); 
		if(books.isEmpty()) throw new NotFoundException(String.format("There are no books published after %s!", date));
		List<BookDto> booksDto = new ArrayList<BookDto>();
		for (Book book : books) {
			booksDto.add(mapstructMapper.bookToBookDto(book));
		}
		return booksDto; 
	}

	@Override
	@Transactional(readOnly = true)
	public List<BookDto> findBooksTitleContains(String title) {
		List<Book> books = bookRepository.findByTitleContains(title); 
		if(books.isEmpty())  throw new NotFoundException(String.format("There are no books with %s in the title!", title));
		List<BookDto> booksDto = new ArrayList<BookDto>();
		for (Book book : books) {
			booksDto.add(mapstructMapper.bookToBookDto(book));
		}
		return booksDto; 
	}

	@Override
	@Transactional(readOnly = true)
	public List<AuthorDto> selAllAuthors() {
		List<Author> allAuthors = authorRepository.findAll();
		if(allAuthors.isEmpty()) throw new NotFoundException("There is no author!");
		List<AuthorDto> allAuthorsDto = new ArrayList<AuthorDto>();
		for(Author author : allAuthors) {
			allAuthorsDto.add(mapstructMapper.authorToAuthorDto(author));
		}
		return allAuthorsDto;
	}

	@Override
	@Transactional(readOnly = true)
	public AuthorDto selAuthorById(Long id) {
		AuthorDto authorDto = mapstructMapper.authorToAuthorDto(authorRepository.findById(id).orElse(null));
		if(authorDto == null) {
			throw new NotFoundException("Author not present or wrong ID!");
		}
		return authorDto;
	}

	@Override
	public boolean saveAuthor(AuthorSlimDto authorToSave) {
		if(authorRepository.findById(authorToSave.getId()).orElse(null) == null){
			throw new NotFoundException(String.format("It is not possible to modify the author with ID %s because it is not present!", authorToSave.getId()));
		}
		authorRepository.save(mapstructMapper.authorSlimDtoToAuthor(authorToSave));
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public AuthorDto findAuthorByNameAndSurname(String firstName, String lastName) {
		Author author = authorRepository.findByNameLike(firstName, lastName);
		if(author == null) throw new NotFoundException("There is no author with the specified name.");
		AuthorDto authorDto = mapstructMapper.authorToAuthorDto(author);
		return authorDto; 
	}

	public Set<AuthorSlimDto> fillAuthorsSetByStringName(String authorNames) {
		Set<AuthorSlimDto> authors = new HashSet<>();

		String[] nameArray = authorNames.split(",");

		  for (String name : nameArray) {
		    String[] parts = name.trim().split(" ");
		    String firstName = parts[0];
		    String lastName = parts[1];

		    AuthorSlimDto author = new AuthorSlimDto();
		    author.setFirstName(firstName);
		    author.setLastName(lastName);

		    authors.add(author);
		  }
		return authors;
	}
	
	public String fillStringNameBySet(Set<AuthorSlimDto> authors) {
		String authorNames = "";
		Iterator<AuthorSlimDto> it = authors.iterator();
		while (it.hasNext()) {
			AuthorSlimDto author = it.next();
			authorNames = authorNames + author.getFirstName() + " " + author.getLastName();
			if(it.hasNext()) authorNames = authorNames + ", ";
		}
		return authorNames;
	}
}
