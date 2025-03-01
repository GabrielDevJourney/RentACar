package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountAlreadyActiveException extends ValidationException {
	public AccountAlreadyActiveException(Long id) {
		super(String.format("Account with ID %d is already active", id),"Account already active!");
	}
}
