package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountNotActiveException extends ValidationException {
	public AccountNotActiveException(Long id) {
		super(String.format("Account with ID: %d is not active so cant rent",id),"This account isn't active so can't " +
				"rent!");
	}
}
