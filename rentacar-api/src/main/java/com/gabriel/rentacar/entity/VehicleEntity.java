package com.gabriel.rentacar.entity;
import com.gabriel.rentacar.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vehicles")
public class VehicleEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "brand", nullable = false)
	private String brand;

	@Column(name = "model")
	private String model;

	@Column(name = "color")
	private String color;

	@Column(name = "year")
	private int yearManufacture;

	@Column(name = "plate", nullable = false, unique = true)
	private String plate;

	@Column(name = "daily_rate")
	private double dailyRate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private VehicleStatus status = VehicleStatus.AVAILABLE;

	@Column(name = "current_kilometers", nullable = false)
	private int currentKilometers;

	@Column(name = "maintenance_end_date")
	private LocalDate maintenanceEndDate;

	@Column(name = "maintenance_kilometers")
	private int maintenanceKilometers;

}