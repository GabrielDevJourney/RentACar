package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ResourceNotFoundException;

public class VehicleNotFoundException extends ResourceNotFoundException {
	public VehicleNotFoundException(Long id) {
		super("Vehicle not found with id: " + id);
	}
}