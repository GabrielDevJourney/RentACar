package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidAgeException extends ValidationException {
	public AccountInvalidAgeException(Long id ) {
		super("Invalid age for ID: " + id, "Please enter a valid age from 18 - 99 years!");
	}
	public AccountInvalidAgeException(String email ) {
		super("Invalid age for account creation with email: " + email, "Please enter a valid age from 18 - 99 years!");
	}
}
