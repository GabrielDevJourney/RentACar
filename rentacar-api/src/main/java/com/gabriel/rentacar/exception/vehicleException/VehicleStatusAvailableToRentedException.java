package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleStatusAvailableToRentedException extends ValidationException {
	public VehicleStatusAvailableToRentedException(Long id) {
		super(String.format("Trying to set available to rented for vehicle with ID: %d",id), "This vehicle isn't " +
				"available" +
				" to be " +
				"rented!");
	}
}
