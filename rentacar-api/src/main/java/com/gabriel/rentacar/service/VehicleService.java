package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.vehicle.VehicleDto;
import com.gabriel.rentacar.entity.VehicleEntity;
import com.gabriel.rentacar.enums.VehicleStatus;
import com.gabriel.rentacar.exception.vehicleException.*;
import com.gabriel.rentacar.mapper.VehicleMapper;
import com.gabriel.rentacar.repository.VehicleRepository;
import com.gabriel.rentacar.utils.PlateValidation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
	private final VehicleRepository vehicleRepository;
	private final VehicleMapper vehicleMapper;
	private final PlateValidation plateValidator;

	public VehicleService(VehicleRepository vehicleRepository,
	                      VehicleMapper vehicleMapper, PlateValidation plateValidator) {
		this.vehicleRepository = vehicleRepository;
		this.vehicleMapper = vehicleMapper;
		this.plateValidator = plateValidator;
	}

	public void createVehicle(VehicleDto vehicleDto) {
		String plate = vehicleDto.getPlate();
		int yearOfManufacture = vehicleDto.getYearManufacture();

		String normalizedPlate = plateValidator.validatePlateFormat(plate,yearOfManufacture);

		if (vehicleRepository.existsByPlate(normalizedPlate)) {
			throw new VehicleLicensePlateAlreadyExistsException(normalizedPlate);
		}
		checkYearOfManufacture(yearOfManufacture);
		normalizeVehicleData(vehicleDto);

		save(vehicleDto);
	}

	public VehicleDto findByPlate(String plate) {
		return vehicleRepository.findByPlate(plate)
				.map(vehicleMapper::toDto)
				.orElse(null);
	}

	public void updateVehicleStatus(Long vehicleId, VehicleStatus newStatus) {
		VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
				.orElseThrow(() -> new VehicleNotFoundException(vehicleId));

		switch (newStatus) {
			case MAINTENANCE:
				setVehicleStatusToMaintenance(vehicle);
				break;
			case DISABLE:
				setVehicleStatusToDisable(vehicle);
				break;
			case AVAILABLE:
				setVehicleStatusToAvailable(vehicle);
				break;
			case RENTED:
				setVehicleStatusToRented(vehicle);
				break;
			default:
				throw new VehicleInvalidStatusUpdateException(vehicleId,vehicle.getStatus(), newStatus);
		}

		vehicleRepository.save(vehicle);
	}


	//* HELPERS PUBLIC METHODS
	public void save(VehicleDto vehicleDto) {
		vehicleRepository.save(vehicleMapper.toEntity(vehicleDto));
	}

	public Optional<VehicleDto> findById(Long id) {
		return vehicleRepository.findById(id)
				.map(vehicleMapper::toDto);
	}

	public List<VehicleDto> findAll() {
		return vehicleMapper.toDtoList(vehicleRepository.findAll());
	}
	public void setVehicleStatusToRented(VehicleEntity vehicle){
		if(vehicle.getStatus() != VehicleStatus.AVAILABLE){
			throw new VehicleStatusAvailableToRentedException(vehicle.getId());
		}
		vehicle.setStatus(VehicleStatus.RENTED);
		vehicleRepository.save(vehicle);
	}

	public void setVehicleStatusToMaintenance(VehicleEntity vehicle){
		if(vehicle.getStatus().equals(VehicleStatus.DISABLE) ){
			throw new VehicleStatusDisableToMaintenanceException(vehicle.getId());
		}
		vehicle.setStatus(VehicleStatus.MAINTENANCE);
		vehicle.setMaintenanceEndDate(LocalDate.now().plusDays(2));
	}

	public void setVehicleStatusToDisable(VehicleEntity vehicle){
		if(vehicle.getStatus().equals(VehicleStatus.RENTED)){
			throw new VehicleStatusRentedToDisableException(vehicle.getId());
		}
		vehicle.setStatus(VehicleStatus.DISABLE);
	}
	public void setVehicleStatusToAvailable(VehicleEntity vehicle){
		if(vehicle.getStatus().equals(VehicleStatus.RENTED)){
			throw new VehicleInvalidStatusUpdateException(vehicle.getId(),vehicle.getStatus(), VehicleStatus.AVAILABLE);
		}
		vehicle.setStatus(VehicleStatus.AVAILABLE);
	}

	//check every day for vehicles that can be updated to available
	@Scheduled(fixedRate = 86400000)
	public void updateMaintenanceVehicles() {
		List<VehicleEntity> vehiclesInMaintenance = vehicleRepository.findAllByStatus(VehicleStatus.MAINTENANCE);
		LocalDate today = LocalDate.now();

		for (VehicleEntity vehicle : vehiclesInMaintenance) {
			if (vehicle.getMaintenanceEndDate().isBefore(today) || vehicle.getMaintenanceEndDate().isEqual(today)) {
				vehicle.setStatus(VehicleStatus.AVAILABLE);
				vehicle.setMaintenanceEndDate(null);
				vehicleRepository.save(vehicle);
			}
		}
	}

	public void checkAndScheduleMaintenanceIfNeeded(VehicleEntity vehicle, int startKilometers, int endKilometers) {
		int distanceTraveled = endKilometers - startKilometers;

		if (distanceTraveled >= vehicle.getMaintenanceKilometers()) {
			setVehicleStatusToMaintenance(vehicle);
			vehicle.setCurrentKilometers(endKilometers);
		} else {
			completeRentalAndStatusToAvailable(vehicle,endKilometers);
		}

		vehicleRepository.save(vehicle);
	}

	public void completeRentalAndStatusToAvailable(VehicleEntity vehicle, int endKilometers) {

		vehicle.setCurrentKilometers(endKilometers);
		vehicle.setStatus(VehicleStatus.AVAILABLE);
	}

	//helper private methods
	
	private void checkYearOfManufacture(int vehicleYear){
		int maxYear = Year.now().getValue();
		int minYear = maxYear - 20;
		if(vehicleYear > maxYear || vehicleYear < minYear){
			throw new VehicleInvalidYearOfManufacture(vehicleYear,minYear, maxYear);
		}
	}

	private VehicleDto normalizeVehicleData(VehicleDto vehicleDto) {
		// easier storage normalizing to one case only for brand names and models
		if (vehicleDto.getBrand() != null) {
			vehicleDto.setBrand(vehicleDto.getBrand().trim().toUpperCase());
		}

		if (vehicleDto.getModel() != null) {
			vehicleDto.setModel(vehicleDto.getModel().trim().toUpperCase());
		}

		if (vehicleDto.getColor() != null) {
			vehicleDto.setColor(vehicleDto.getColor().trim().toLowerCase());
		}
		return vehicleDto;
	}
}