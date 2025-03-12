package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidAuthException extends ValidationException {
	public AccountInvalidAuthException() {
		super("InvalidAuth failed: Invalid credentials",
				"Invalid email or password");
	}
}