package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {
	public AccountNotFoundException(Long id) {
		super(String.format("Account not found with ID: %d ", id));
	}
}
