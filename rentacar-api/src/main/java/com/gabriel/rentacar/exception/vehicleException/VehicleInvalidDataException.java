package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleInvalidDataException extends ValidationException {
	public VehicleInvalidDataException(String field,String message) {
      super(String.format("Invalid vehicle data: %s - %s", field, message),
              String.format("Please provide valid %s information", field));
	}
}
