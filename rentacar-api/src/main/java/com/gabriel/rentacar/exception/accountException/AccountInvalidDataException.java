package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidDataException extends ValidationException {
	public AccountInvalidDataException(String field, String message) {
		super(String.format("Invalid account data: %s - %s", field, message),
				String.format("Please provide valid %s information", field));
	}
}