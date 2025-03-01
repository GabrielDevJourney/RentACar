package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleStatusRentedToDisableException extends ValidationException {
	public VehicleStatusRentedToDisableException(Long vehicleId) {
		super("Can't set status of disable to rented vehicle of ID:" + vehicleId, "Can't make status of rented become" +
				" disable");
	}
}
