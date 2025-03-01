package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.exception.ValidationException;

public class VehicleNotAvailableException extends ValidationException {
  public VehicleNotAvailableException(Long id) {
    super("Vehicle not available with id: " + id,"Vehicle is not available!");
  }
}