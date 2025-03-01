package com.gabriel.rentacar.dto.vehicle;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

	@NotBlank(message = "Plate is required")
	@Pattern(regexp = "^[A-Z]{2}-[0-9]{2}-[A-Z]{2}$",
			message = "Invalid plate format. Must match Portuguese format (e.g., AA-12-BB)")
	private String plate;

	@NotBlank(message = "Brand is required")
	@Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
	private String brand;

	@Size(max = 50, message = "Model must be up to 50 characters")
	private String model;

	@Size(max = 30, message = "Color must be up to 30 characters")
	private String color;

	@NotNull(message = "Year of manufacture required")
	private int yearManufacture;

	@Range(min = 20, max = 1000, message = "Please enter a proper daily rate")
	private double dailyRate;

	@Min(value = 0, message = "Kilometers must be positive")
	private int currentKilometers;

	@Range(min = 5000, max = 10000, message = "Maintenance kilometers must be between 5000km - 10000km")
	private int maintenanceKilometers;
}