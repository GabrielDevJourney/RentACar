package com.gabriel.rentacar.repository;

import com.gabriel.rentacar.entity.VehicleEntity;
import com.gabriel.rentacar.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
	boolean existsByPlate(String plate);

	Optional<VehicleEntity> findByPlate(String plate);

	List<VehicleEntity>findAllByStatus(VehicleStatus status);

}
