package com.gabriel.rentacar.exception.rentalException;

import com.gabriel.rentacar.exception.ValidationException;

public class RentalInvalidReturningEndKilometersException extends ValidationException {
	public RentalInvalidReturningEndKilometersException(Long id) {
		super(String.format("Invalid end kilometers for rent %d",id),"End kilometers must be above start kilometers");
	}
}
