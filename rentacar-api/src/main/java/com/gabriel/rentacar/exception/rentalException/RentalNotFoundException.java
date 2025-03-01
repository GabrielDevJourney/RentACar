package com.gabriel.rentacar.exception.rentalException;

import com.gabriel.rentacar.exception.ResourceNotFoundException;

public class RentalNotFoundException extends ResourceNotFoundException {
	public RentalNotFoundException(Long id) {
		super("Rental not found with id: " + id);
	}
}
