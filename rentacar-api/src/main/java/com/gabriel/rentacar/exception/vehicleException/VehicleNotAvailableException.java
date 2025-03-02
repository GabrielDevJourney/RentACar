package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleNotAvailableException extends ValidationException {
  public VehicleNotAvailableException(Long id) {
    super(String.format("Vehicle not available with ID: %d ", id),"Vehicle is not available!");
  }
}