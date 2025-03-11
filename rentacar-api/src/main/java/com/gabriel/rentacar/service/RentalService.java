package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.rent.RentalRequestDto;
import com.gabriel.rentacar.dto.rent.RentalResponseDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.entity.RentalEntity;
import com.gabriel.rentacar.entity.VehicleEntity;
import com.gabriel.rentacar.enums.RentalStatus;
import com.gabriel.rentacar.exception.accountException.AccountNotActiveException;
import com.gabriel.rentacar.exception.accountException.AccountNotFoundException;
import com.gabriel.rentacar.exception.rentalException.RentalInvalidReturningEndKilometersException;
import com.gabriel.rentacar.exception.rentalException.RentalNotFoundException;
import com.gabriel.rentacar.exception.vehicleException.VehicleNotFoundException;
import com.gabriel.rentacar.mapper.RentalMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import com.gabriel.rentacar.repository.RentalRepository;
import com.gabriel.rentacar.repository.VehicleRepository;
import com.gabriel.rentacar.utils.DateValidation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalService {
	private final RentalRepository rentalRepository;
	private final RentalMapper rentalMapper;
	private final VehicleRepository vehicleRepository;
	private final AccountRepository accountRepository;
	private final VehicleService vehicleService;
	private final DateValidation dateValidator;

	public RentalService(RentalRepository rentalRepository, RentalMapper rentalMapper, VehicleRepository vehicleRepository,
	                     AccountRepository accountRepository, VehicleService vehicleService,DateValidation dateValidator) {
		this.rentalRepository = rentalRepository;
		this.rentalMapper = rentalMapper;
		this.vehicleRepository = vehicleRepository;
		this.accountRepository =accountRepository;
		this.vehicleService = vehicleService;
		this.dateValidator = dateValidator;
	}

	public void createRenting(RentalRequestDto rentalRequestDto){
		Long vehicleId = rentalRequestDto.getVehicleId();
		Long accountId = rentalRequestDto.getAccountId();

		VehicleEntity vehicle =
				vehicleRepository.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));
		AccountEntity account =
				accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

		validateAccountIsActive(account);

		dateValidator.validateRentalDates(
				rentalRequestDto.getVehicleId(),
				rentalRequestDto.getDateStart(),
				rentalRequestDto.getDateEnd()
		);

		vehicleService.updateVehicleStatusToRented(vehicle);

		RentalEntity rentalEntity = rentalMapper.toEntityRequest(rentalRequestDto);

		rentalEntity.setAccountEntity(account);
		rentalEntity.setVehicleEntity(vehicle);
		rentalEntity.setStartKilometers(vehicle.getCurrentKilometers());

		rentalRepository.save(rentalEntity);
	}

	public void endRenting(Long id, int rentalReturnKilometers) {
		RentalEntity rental = findActiveRentalById(id);

		validateReturnKilometers(rental, rentalReturnKilometers);

		LocalDate returnDate = LocalDate.now();
		rental.setEndKilometers(rentalReturnKilometers);
		rental.setDateReturn(returnDate);
		rental.setStatus(RentalStatus.COMPLETED);

		double totalPrice = calculateRentFinalPrice(
				rental.getVehicleEntity().getDailyRate(),
				rental.getDateStart(),
				returnDate
		);
		rental.setTotalPrice(totalPrice);

		vehicleService.completeRental(
				rental.getVehicleEntity(),
				rental.getStartKilometers(),
				rentalReturnKilometers
		);

		rentalRepository.save(rental);
	}

	// Http methods for controller
	public RentalResponseDto getRentingInfo(Long id){
		RentalEntity rent = rentalRepository.findById(id).orElseThrow(() -> new RentalNotFoundException(id));
		return rentalMapper.toDtoResponse(rent);
	}

	public RentalResponseDto getRentingInfoByVehicleId(Long id){
		RentalEntity rent = rentalRepository.findByVehicleEntity_Id(id);
		return rentalMapper.toDtoResponse(rent);
	}

	public RentalResponseDto getRentingInfoByAccountId(Long id){
		RentalEntity rent = rentalRepository.findByAccountEntity_Id(id);
		return rentalMapper.toDtoResponse(rent);
	}

	public RentalResponseDto getRentingInfoByVehicleIdAndStatus(Long id, RentalStatus status){
		RentalEntity rent = rentalRepository.findByVehicleEntity_IdAndStatus(id,status);
		if(rent.getStatus() != status){
			return null;
		}
		return rentalMapper.toDtoResponse(rent);
	}

	public RentalResponseDto getRentingInfoByAccountIdAndStatus(Long id, RentalStatus status){
		RentalEntity rent = rentalRepository.findByAccountEntity_IdAndStatus(id,status);
		if(rent.getStatus() != status){
			return null;
		}
		return rentalMapper.toDtoResponse(rent);
	}

	public List<RentalResponseDto> getAllRentalsForAccount(Long id){
		List<RentalEntity> rentals = rentalRepository.findAllByAccountEntity_Id(id);
		List<RentalResponseDto> rentalsDtos = new ArrayList<>();
		for(RentalEntity entity : rentals){
			rentalsDtos.add(rentalMapper.toDtoResponse(entity));
		}
		return rentalsDtos;
	}

	public List<RentalResponseDto> getAllRentalsForVehicle(Long id){
		List<RentalEntity> rentals = rentalRepository.findAllByVehicleEntity_Id(id);
		List<RentalResponseDto> rentalsDtos = new ArrayList<>();
		for(RentalEntity entity : rentals){
			rentalsDtos.add(rentalMapper.toDtoResponse(entity));
		}
		return rentalsDtos;
	}

	public List<RentalResponseDto> getAllRentalsOfStatus(RentalStatus status){
		List<RentalEntity> rentals = rentalRepository.findAllByStatus(status);
		List<RentalResponseDto> rentalsDtos = new ArrayList<>();
		for(RentalEntity entity : rentals){
			rentalsDtos.add(rentalMapper.toDtoResponse(entity));
		}
		return rentalsDtos;
	}


	//* PRIVATE HELPER METHODS
	private double calculateRentFinalPrice(double vehicleDailyPrice,LocalDate startDate, LocalDate endDate){

		 return  vehicleDailyPrice * calculateRentTotalDays(startDate,endDate);
	}
	private long calculateRentTotalDays(LocalDate startDate, LocalDate endDate){
		return startDate.until(endDate, ChronoUnit.DAYS);
	}
	private RentalEntity findActiveRentalById(Long id) {
		RentalEntity rental = rentalRepository.findByIdAndStatus(id, RentalStatus.ACTIVE);
		if (rental == null) {
			throw new RentalNotFoundException(id);
		}
		return rental;
	}

	private void validateReturnKilometers(RentalEntity rental, int returnKilometers) {
		if (returnKilometers <= rental.getStartKilometers()) {
			throw new RentalInvalidReturningEndKilometersException(rental.getId());
		}
	}

	private void validateAccountIsActive(AccountEntity account){
		if(account.isActive()){
			return;
		}
		throw new AccountNotActiveException(account.getId());
	}

}
