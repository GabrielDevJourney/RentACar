package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountAlreadyDeactivatedException extends ValidationException {
	public AccountAlreadyDeactivatedException(Long id) {
		super(String.format("Account with ID %d is already deactivated", id), "Account already deactivated!");
	}
}
