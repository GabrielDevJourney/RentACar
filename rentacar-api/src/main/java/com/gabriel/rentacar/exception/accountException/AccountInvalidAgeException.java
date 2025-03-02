package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidAgeException extends ValidationException {
	public AccountInvalidAgeException(Long id ) {
		super(String.format("Invalid age for ID: %d",id), "Please enter a valid age from 18 - 99 years!");
	}
	public AccountInvalidAgeException(String email ) {
		super(String.format("Invalid age for account creation with email: %s ",email), "Please enter a valid age from" +
				" " +
				"18 - " +
				"99 " +
				"years!");
	}
}
