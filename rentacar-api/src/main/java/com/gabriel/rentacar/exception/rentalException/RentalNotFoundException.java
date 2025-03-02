package com.gabriel.rentacar.exception.rentalException;

import com.gabriel.rentacar.exception.ResourceNotFoundException;

public class RentalNotFoundException extends ResourceNotFoundException {
	public RentalNotFoundException(Long id) {
		super(String.format("Rental not found with ID: %d ",id));
	}
}
