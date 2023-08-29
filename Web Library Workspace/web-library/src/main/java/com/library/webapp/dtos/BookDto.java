package com.library.webapp.dtos;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.library.webapp.validator.IsbnCodeConstraint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookDto {
	
	@IsbnCodeConstraint
	private String isbn;
	
	@NotEmpty(message = "Title cannot be empty.")
	private String title;
	
	@NotEmpty(message = "Genre cannot be empty.")
	private String genre;
	
	@Size(min = 1, max = 2000, message = "The plot of the book must be between 1 and 2000 words.")
	private String plot;
	
	@NotNull(message = "Publication date cannot be empty.") 
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate publicationDate;
		
	@Min(value = 1, message = "Must be equal or greater than 1.")
	private int numberPages;
	
	private Set<AuthorSlimDto> authors;
	
	public BookDto() {
		super();
		this.authors = new HashSet<>();
	}

	public BookDto(String isbn, String title, String genre, String plot, LocalDate publicationDate, int numberPages,
			Set<AuthorSlimDto> authors) {
		super();
		this.isbn = isbn;
		this.title = title;
		this.genre = genre;
		this.plot = plot;
		this.publicationDate = publicationDate;
		this.numberPages = numberPages;
		this.authors = authors;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getPlot() {
		return plot;
	}

	public void setPlot(String plot) {
		this.plot = plot;
	}

	public LocalDate getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(LocalDate publicationDate) {
		this.publicationDate = publicationDate;
	}

	public int getNumberPages() {
		return numberPages;
	}

	public void setNumberPages(int numberPages) {
		this.numberPages = numberPages;
	}

	public Set<AuthorSlimDto> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<AuthorSlimDto> authors) {
		this.authors = authors;
	}

	@Override
	public int hashCode() {
		return Objects.hash(isbn);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookDto other = (BookDto) obj;
		return Objects.equals(isbn, other.isbn);
	}
	
}
