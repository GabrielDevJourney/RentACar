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

	//* HTTP REQUESTS BUSINESS LOGIC
	public void createVehicle(VehicleDto vehicleDto) {
		validateNotNull(vehicleDto, "vehicleDto");
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
		validateNotNull(plate, "plate");

		return vehicleRepository.findByPlate(plate)
				.map(vehicleMapper::toDto)
				.orElse(null);
	}

	public void updateVehicleStatus(Long vehicleId, VehicleStatus newStatus) {

		validateNotNull(vehicleId,"vehicle id");
		validateNotNull(newStatus, "new vehicle status");

		VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
				.orElseThrow(() -> new VehicleNotFoundException(vehicleId));

		validateStatusTransition(vehicle.getStatus(), newStatus, vehicleId);

		switch (newStatus) {
			case MAINTENANCE:
				updateVehicleStatusToMaintenance(vehicle);
				break;
			case DISABLE:
				updateVehicleStatusToDisable(vehicle);
				break;
			case AVAILABLE:
				updateVehicleStatusToAvailable(vehicle);
				break;
			case RENTED:
				updateVehicleStatusToRented(vehicle);
				break;
			default:
				throw new VehicleInvalidStatusUpdateException(vehicleId,vehicle.getStatus(), newStatus);
		}
	}

	public VehicleDto getVehicleById(Long vehicleId){
		VehicleEntity vehicleEntity = vehicleRepository.findById(vehicleId).orElseThrow(() -> {
			throw new VehicleNotFoundException(vehicleId);
		});
		return vehicleMapper.toDto(vehicleEntity);
	}

	public List<VehicleDto> getAllVehicles(){
		return vehicleMapper.toDtoList(vehicleRepository.findAll());
	}

	//* HELPERS PUBLIC METHODS
	public void save(VehicleDto vehicleDto) {
		vehicleRepository.save(vehicleMapper.toEntity(vehicleDto));
	}

	public Optional<VehicleDto> findById(Long id) {
		return vehicleRepository.findById(id)
				.map(vehicleMapper::toDto);
	}

	//*UPDATE STATUS METHOD HELPERS

	public void updateVehicleStatusToRented(VehicleEntity vehicle) {
		validateNotNull(vehicle, "vehicle");
		validateStatusTransition(vehicle.getStatus(), VehicleStatus.RENTED, vehicle.getId());

		vehicle.setStatus(VehicleStatus.RENTED);
		vehicleRepository.save(vehicle);
	}

	public void updateVehicleStatusToMaintenance(VehicleEntity vehicle) {
		validateNotNull(vehicle, "vehicle");
		validateStatusTransition(vehicle.getStatus(), VehicleStatus.MAINTENANCE, vehicle.getId());

		vehicle.setStatus(VehicleStatus.MAINTENANCE);
		vehicle.setMaintenanceEndDate(LocalDate.now().plusDays(2));
		vehicleRepository.save(vehicle);
	}

	public void updateVehicleStatusToDisable(VehicleEntity vehicle) {
		validateNotNull(vehicle, "vehicle");
		validateStatusTransition(vehicle.getStatus(), VehicleStatus.DISABLE, vehicle.getId());

		vehicle.setStatus(VehicleStatus.DISABLE);
		vehicleRepository.save(vehicle);
	}

	public void updateVehicleStatusToAvailable(VehicleEntity vehicle) {
		validateNotNull(vehicle, "vehicle");
		validateStatusTransition(vehicle.getStatus(), VehicleStatus.AVAILABLE, vehicle.getId());

		vehicle.setStatus(VehicleStatus.AVAILABLE);

		if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
			vehicle.setMaintenanceEndDate(null);
		}

		vehicleRepository.save(vehicle);
	}


	//check every day for vehicles that can be updated to available
	@Scheduled(fixedRate = 86400000)//milliseconds
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

	public void completeRental(VehicleEntity vehicle, int rentalStartKilometers, int endKilometers) {
		int distanceTraveled = endKilometers - rentalStartKilometers;
		vehicle.setCurrentKilometers(endKilometers);

		if (distanceTraveled >= vehicle.getMaintenanceKilometers()) {
			vehicle.setStatus(VehicleStatus.MAINTENANCE);
			vehicle.setMaintenanceEndDate(LocalDate.now().plusDays(2));
		} else {
			vehicle.setStatus(VehicleStatus.AVAILABLE);
		}

		vehicleRepository.save(vehicle);
	}

	//* PRIVATE HELPER METHODS
	
	private void checkYearOfManufacture(int vehicleYear){
		int maxYear = Year.now().getValue();
		int minYear = maxYear - 20;
		if(vehicleYear > maxYear || vehicleYear < minYear){
			throw new VehicleInvalidYearOfManufacture(vehicleYear,minYear, maxYear);
		}
	}

	private void normalizeVehicleData(VehicleDto vehicleDto) {
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
	}

	private void validateNotNull(Object obj, String fieldName) {
		if (obj == null) {
			throw new VehicleInvalidDataException(fieldName, fieldName + " cannot be null");
		}
	}

	private void validateStatusTransition(VehicleStatus currentStatus, VehicleStatus newStatus, Long vehicleId) {
		if (currentStatus == newStatus) {
			return; // No transition needed
		}

		boolean isValid = switch (currentStatus) {
			case AVAILABLE -> (newStatus == VehicleStatus.RENTED ||
					newStatus == VehicleStatus.MAINTENANCE ||
					newStatus == VehicleStatus.DISABLE);
			case RENTED -> {
				if (newStatus == VehicleStatus.DISABLE) {
					throw new VehicleStatusRentedToDisableException(vehicleId);
				}
				yield (newStatus == VehicleStatus.AVAILABLE ||
						newStatus == VehicleStatus.MAINTENANCE);
			}
			case MAINTENANCE -> (newStatus == VehicleStatus.AVAILABLE ||
					newStatus == VehicleStatus.DISABLE);
			case DISABLE -> {
				if (newStatus == VehicleStatus.MAINTENANCE) {
					throw new VehicleStatusDisableToMaintenanceException(vehicleId);
				}
				yield (newStatus == VehicleStatus.AVAILABLE);
			}
		};

		if (!isValid) {
			throw new VehicleInvalidStatusUpdateException(vehicleId, currentStatus, newStatus);
		}
	}
}