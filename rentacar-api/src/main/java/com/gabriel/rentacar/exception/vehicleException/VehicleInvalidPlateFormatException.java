package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleInvalidPlateFormatException extends ValidationException {
	public VehicleInvalidPlateFormatException(String plate,String message) {
		super(String.format("Invalid vehicle plate: %s - %s", plate, message),
				String.format(plate + " " + message));	}
}
