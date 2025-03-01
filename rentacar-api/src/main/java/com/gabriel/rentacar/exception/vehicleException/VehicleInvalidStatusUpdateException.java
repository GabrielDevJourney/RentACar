package com.gabriel.rentacar.exception.vehicleException;

import com.gabriel.rentacar.enums.VehicleStatus;
import com.gabriel.rentacar.exception.ValidationException;

public class VehicleInvalidStatusUpdateException extends ValidationException {
	private final VehicleStatus currentStatus;
	private final VehicleStatus newStatus;

	public VehicleInvalidStatusUpdateException(Long vehicleId,VehicleStatus currentStatus, VehicleStatus newStatus) {
		super(String.format("Invalid status transition from %s to %s in vehicle ID: %s", currentStatus, newStatus,vehicleId),
				String.format(
				"Invalid status transition from %s to %s", currentStatus, newStatus));
		this.currentStatus = currentStatus;
		this.newStatus = newStatus;
	}

	public VehicleStatus getCurrentStatus() {
		return currentStatus;
	}

	public VehicleStatus getNewStatus() {
		return newStatus;
	}
}