package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleLicensePlateAlreadyExistsException extends ValidationException {
	public VehicleLicensePlateAlreadyExistsException(String plate) {
		super(String.format("License plate already exists: $s" ,plate),"Invalid plate!");
	}
}