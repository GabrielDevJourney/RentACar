package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {
	public AccountNotFoundException(Long id) {
		super("Account not found with id: " + id);
	}
}
