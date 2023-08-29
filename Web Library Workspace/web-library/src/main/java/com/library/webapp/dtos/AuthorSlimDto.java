package com.library.webapp.dtos;

import java.util.Objects;

import jakarta.validation.constraints.NotEmpty;

public class AuthorSlimDto {
	private Long id;
	
	@NotEmpty(message = "First name cannot be empty.")
	private String firstName;
	
	@NotEmpty(message = "Last name cannot be empty.")
	private String lastName;

	public AuthorSlimDto() {
		super();
	}

	public AuthorSlimDto(Long id, String firstName, String lastName) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
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
		AuthorSlimDto other = (AuthorSlimDto) obj;
		return Objects.equals(firstName, other.firstName) && Objects.equals(lastName, other.lastName);
	}

}