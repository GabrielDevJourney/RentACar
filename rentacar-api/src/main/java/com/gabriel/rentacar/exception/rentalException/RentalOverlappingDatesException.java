package com.gabriel.rentacar.exception.rentalException;

import com.gabriel.rentacar.exception.ValidationException;

import java.time.LocalDate;

public class RentalOverlappingDatesException extends ValidationException {
	public RentalOverlappingDatesException(Long id, LocalDate startDate, LocalDate endDate) {
		super(String.format("%d cant be rented for start date %tF and end date %tF ",id,startDate,endDate), "Vehicle " +
                "already rented " +
                "for this " +
                "date!");
	}
}
