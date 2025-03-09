package com.gabriel.rentacar.exception.rentalException;

import com.gabriel.rentacar.exception.ValidationException;

public class RentalInvalidRentingDatesException extends ValidationException {
	public RentalInvalidRentingDatesException(String message) {
		super(message,"Invalid dates for this renting");
	}
	public RentalInvalidRentingDatesException(String messageLog, String clientMessage){
		super(messageLog,clientMessage);
	}
}
