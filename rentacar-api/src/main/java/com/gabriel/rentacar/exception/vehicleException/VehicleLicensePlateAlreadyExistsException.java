package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleLicensePlateAlreadyExistsException extends ValidationException {
	public VehicleLicensePlateAlreadyExistsException(String plate) {
		super("License plate already exists: " + plate,"Invalid plate!");
	}
}