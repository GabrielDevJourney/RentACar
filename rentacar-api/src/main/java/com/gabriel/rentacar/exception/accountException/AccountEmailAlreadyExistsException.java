package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountEmailAlreadyExistsException extends ValidationException {
	public AccountEmailAlreadyExistsException(String email) {
		super("Email already exists: " + email,"Invalid email!");
	}
}