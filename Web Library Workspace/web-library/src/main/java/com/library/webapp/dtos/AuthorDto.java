package com.library.webapp.dtos;

import java.util.Objects;
import java.util.Set;

public class AuthorDto {
	private Long id;
	private String firstName;
	private String lastName;
	private Set<BookSlimDto> books;
	
	public AuthorDto() {
		super();
	}

	public AuthorDto(Long id, String firstName, String lastName, Set<BookSlimDto> books) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.books = books;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Set<BookSlimDto> getBooks() {
		return books;
	}

	public void setBooks(Set<BookSlimDto> books) {
		this.books = books;
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorDto other = (AuthorDto) obj;
		return Objects.equals(firstName, other.firstName) && Objects.equals(lastName, other.lastName);
	}
	
}
