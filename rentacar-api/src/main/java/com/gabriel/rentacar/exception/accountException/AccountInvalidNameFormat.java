package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountInvalidNameFormat extends ValidationException {
	public AccountInvalidNameFormat(String fieldName,String reason) {
		super(String.format("Invalid %s format: %s", fieldName, reason),
				String.format("Please input a valid %s", fieldName.toLowerCase()));
	}
}
