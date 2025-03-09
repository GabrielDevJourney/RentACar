package com.gabriel.rentacar.dto.rent;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestDto {
	@NotNull(message = "Account ID is required")
	private Long accountId;

	@NotNull(message = "Vehicle ID is required")
	private Long vehicleId;

	@NotNull(message = "Start date is required")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateStart;

	@NotNull(message = "End date is required")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateEnd;
}
