package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidNumberException extends ValidationException {
	public AccountInvalidNumberException(String phoneNumber) {
		super(String.format(" %s is an invalid format. Ensure 91,92,93,96!", phoneNumber),"Phone number with wrong " +
				"format " +
				"ensure" +
				" " +
				"91/92/93/96xxxxxxx");
	}
}
