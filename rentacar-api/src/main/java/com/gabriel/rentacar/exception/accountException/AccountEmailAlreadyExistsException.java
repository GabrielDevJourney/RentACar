package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountEmailAlreadyExistsException extends ValidationException {
	public AccountEmailAlreadyExistsException(String email) {
		super(String.format("Email already exists: %s ", email),"Invalid email, already registered");
	}
}