package com.library.webapp.dtos;

import java.util.Objects;

public class BookSlimDto {
	private String isbn;
	private String title;
	
	public BookSlimDto() {
		super();
	}

	public BookSlimDto(String isbn, String title) {
		super();
		this.isbn = isbn;
		this.title = title;
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
		BookSlimDto other = (BookSlimDto) obj;
		return Objects.equals(isbn, other.isbn);
	}
}
