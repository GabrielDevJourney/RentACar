package com.gabriel.rentacar.controller;

import com.gabriel.rentacar.dto.vehicle.VehicleDto;
import com.gabriel.rentacar.enums.VehicleStatus;
import com.gabriel.rentacar.exception.vehicleException.VehicleNotFoundException;
import com.gabriel.rentacar.service.VehicleService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
	private final VehicleService vehicleService;

	public VehicleController(VehicleService vehicleService) {
		this.vehicleService = vehicleService;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@GetMapping("/search/{plate}")
	public ResponseEntity<VehicleDto> getVehicleByPlate(@PathVariable String plate) {
		VehicleDto vehicle = vehicleService.findByPlate(plate);
		return vehicle != null ? ResponseEntity.ok(vehicle) : ResponseEntity.notFound().build();
	}
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@GetMapping("/search/{vehicleId}")
	public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long vehicleId) {
		VehicleDto vehicle = vehicleService.getVehicleById(vehicleId);
		return vehicle != null ? ResponseEntity.ok(vehicle) : ResponseEntity.notFound().build();
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@GetMapping("/{vehicleId}/plate")
	public ResponseEntity<String> getPlateById(@PathVariable Long vehicleId) {
		VehicleDto vehicle = vehicleService.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));
		String plate = vehicle.getPlate();
		return plate != null ? ResponseEntity.ok(plate) : ResponseEntity.notFound().build();
	}
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
	@GetMapping
	public ResponseEntity<List<VehicleDto>> getAllVehicles() {
		List<VehicleDto> vehicleDtoList = vehicleService.getAllVehicles();
		return ResponseEntity.ok(vehicleDtoList);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<Void> createVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
		vehicleService.createVehicle(vehicleDto);
		return ResponseEntity.ok().build();
	}
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Transactional
	@PatchMapping("/{vehicleId}/status/{status}")
	public ResponseEntity<Void> updateVehicleStatus(
			@PathVariable Long vehicleId,
			@PathVariable VehicleStatus status) {

		vehicleService.updateVehicleStatus(vehicleId, status);
		return ResponseEntity.ok().build();
	}


}