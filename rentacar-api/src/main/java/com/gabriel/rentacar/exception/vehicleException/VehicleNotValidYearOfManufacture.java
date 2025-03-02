package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleNotValidYearOfManufacture extends ValidationException {
	public VehicleNotValidYearOfManufacture(int vehicleYear,int minYear, int maxYear) {
		super(String.format(vehicleYear + "$d is not a valid year" , vehicleYear),
				String.format("Please insert a valid year between %d and %d",minYear,maxYear));
	}
}
