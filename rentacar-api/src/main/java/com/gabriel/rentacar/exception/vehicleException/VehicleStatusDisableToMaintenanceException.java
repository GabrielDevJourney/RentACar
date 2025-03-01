package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleStatusDisableToMaintenanceException extends ValidationException {
	public VehicleStatusDisableToMaintenanceException(Long id) {
		super("Trying to set maintenance to rented or disable for vehicle with ID: " + id, "This vehicle can't be " +
				"send to maintenance!");
	}
}
