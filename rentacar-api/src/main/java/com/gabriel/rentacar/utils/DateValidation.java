package com.gabriel.rentacar.utils;

import com.gabriel.rentacar.dto.rent.RentalRequestDto;
import com.gabriel.rentacar.entity.RentalEntity;
import com.gabriel.rentacar.exception.rentalException.RentalInvalidRentingDatesException;
import com.gabriel.rentacar.exception.rentalException.RentalOverlappingDatesException;
import com.gabriel.rentacar.repository.RentalRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DateValidation {
	private final RentalRepository rentalRepository;

	public DateValidation(RentalRepository rentalRepository) {
		this.rentalRepository = rentalRepository;
	}

	public void validateRentalDates(Long vehicleId, LocalDate startDate, LocalDate endDate) {
		validateStartDateNotInPast(startDate);
		validateEndDateAfterStartDate(startDate, endDate);
		validateMaxRentingPeriod(startDate, endDate);
		checkOverlappingDates(vehicleId, startDate, endDate);
	}

	private void validateStartDateNotInPast(LocalDate startDate) {
		LocalDate current = LocalDate.now();
		if (startDate.isBefore(current)) {
			throw new RentalInvalidRentingDatesException(
					"Trying to start rental in past!",
					"Start date can't be in past"
			);
		}
	}

	private void validateEndDateAfterStartDate(LocalDate startDate, LocalDate endDate) {
		if (endDate.isBefore(startDate)) {
			throw new RentalInvalidRentingDatesException(
					"Trying to end rent before starting date",
					"End date inserted not allowed"
			);
		}
	}

	private void validateMaxRentingPeriod(LocalDate startDate, LocalDate endDate) {
		long maxRentingDays = startDate.until(endDate, ChronoUnit.DAYS);
		if (maxRentingDays > 30) {
			throw new RentalInvalidRentingDatesException(
					"Max renting days achieved",
					"You can only rent a vehicle for 30 days max"
			);
		}
	}

	private void checkOverlappingDates(Long vehicleId, LocalDate startDate, LocalDate endDate) {
		List<RentalEntity> overlappingRentals = rentalRepository.findOverlappingRentals(
				vehicleId, startDate, endDate
		);

		if (!overlappingRentals.isEmpty()) {
			throw new RentalOverlappingDatesException(vehicleId, startDate, endDate);
		}
	}
}