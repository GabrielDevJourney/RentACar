package com.gabriel.rentacar.entity;

import com.gabriel.rentacar.enums.RentalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rentals")
public class RentalEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity accountEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private VehicleEntity vehicleEntity;

	@Column(name = "date_start", nullable = false)
	private LocalDate dateStart;

	@Column(name = "date_end", nullable = false)
	private LocalDate dateEnd;

	@Column(name = "date_return")
	private LocalDate dateReturn;

	@Column(name = "start_kilometers", nullable = false)
	@Min(value = 0, message = "Kilometers must be above 0")
	private int startKilometers;

	@Column(name = "end_kilometers")
	@Min(value = 0, message = "Kilometers must be above start kilometers")
	private int endKilometers;

	//having also entity check for data persistence and double validation if bypassed by first layer
	@AssertTrue(message = "End kilometers must be greater than start kilometers")
	private boolean isKilometersValid() {
		if (endKilometers == 0) return true;
		return endKilometers >= startKilometers;
	}

	@Column(name = "total_price")
	@Min(value = 0, message = "Must be above zero")
	private double totalPrice;


	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private RentalStatus status = RentalStatus.ACTIVE;

}
