package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.rent.RentalRequestDto;
import com.gabriel.rentacar.dto.rent.RentalResponseDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.entity.RentalEntity;
import com.gabriel.rentacar.entity.VehicleEntity;
import com.gabriel.rentacar.enums.RentalStatus;
import com.gabriel.rentacar.exception.accountException.AccountNotFoundException;
import com.gabriel.rentacar.exception.rentalException.RentalNotFoundException;
import com.gabriel.rentacar.exception.vehicleException.VehicleNotFoundException;
import com.gabriel.rentacar.mapper.RentalMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import com.gabriel.rentacar.repository.RentalRepository;
import com.gabriel.rentacar.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalService {
	private final RentalRepository rentalRepository;
	private final RentalMapper rentalMapper;
	private final VehicleRepository vehicleRepository;
	private final AccountRepository accountRepository;
	private final VehicleService vehicleService;

	public RentalService(RentalRepository rentalRepository, RentalMapper rentalMapper, VehicleRepository vehicleRepository,
	                     AccountRepository accountRepository, VehicleService vehicleService) {
		this.rentalRepository = rentalRepository;
		this.rentalMapper = rentalMapper;
		this.vehicleRepository = vehicleRepository;
		this.accountRepository =accountRepository;
		this.vehicleService = vehicleService;
	}

	//todo make start kilometers be automated to use vehicle currentKilometers
	public void createRenting(RentalRequestDto rentalRequestDto){
		Long vehicleId = rentalRequestDto.getVehicleId();
		Long accountId = rentalRequestDto.getAccountId();

		VehicleEntity vehicle =
				vehicleRepository.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));
		AccountEntity account =
				accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

		checkOverLappingDates(rentalRequestDto);
		vehicleService.setVehicleStatusToRented(vehicle);

		RentalEntity rentalEntity = rentalMapper.toEntityRequest(rentalRequestDto);
		rentalEntity.setAccountEntity(account);
		rentalEntity.setVehicleEntity(vehicle);
		rentalRepository.save(rentalEntity);
	}

	public void endRenting(Long id, int endKilometers) {
		RentalEntity rent = rentalRepository.findByIdAndStatus(id, RentalStatus.ACTIVE);
		if(rent == null){
			throw new RentalNotFoundException(id);
		}
		VehicleEntity vehicle = rent.getVehicleEntity();
		int startKilometers = rent.getStartKilometers();

		rent.setEndKilometers(endKilometers);
		rent.setDateReturn(LocalDate.now());
		rent.setStatus(RentalStatus.COMPLETED);

		vehicleService.completeRental(vehicle,startKilometers,endKilometers);

		rentalRepository.save(rent);
	}

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


	//* HELPER METHODS
	private void checkOverLappingDates(RentalRequestDto rentalRequestDto){
		List<RentalEntity> overlappingRentals = rentalRepository.findOverlappingRentals(rentalRequestDto.getVehicleId(),
				rentalRequestDto.getDateStart(), rentalRequestDto.getDateEnd());

		if(!overlappingRentals.isEmpty()){
			throw new RuntimeException("Vehicle already rented for this date!");
		}
	}


}
