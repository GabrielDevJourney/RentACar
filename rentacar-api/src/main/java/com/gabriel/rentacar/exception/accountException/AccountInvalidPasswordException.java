package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidPasswordException extends ValidationException {
	public AccountInvalidPasswordException(String reason) {
		super(String.format("Invalid password: %s at auth service", reason),
				String.format("Password is invalid: %s", reason));
	}
}
