package com.gabriel.rentacar.exception.rentalException;

import com.gabriel.rentacar.exception.ValidationException;

public class RentalEndKilometersException extends ValidationException {
	public RentalEndKilometersException(int start, int end) {
		super(String.format("End kilometers (%d) must be greater than start kilometers (%d)", end, start),"End " +
				"kilometers " +
				"must be greater then start kilometers");
	}
}
