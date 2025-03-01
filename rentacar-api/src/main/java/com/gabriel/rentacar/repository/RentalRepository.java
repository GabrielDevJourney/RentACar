package com.gabriel.rentacar.repository;

import com.gabriel.rentacar.entity.RentalEntity;
import com.gabriel.rentacar.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<RentalEntity, Long> {
	RentalEntity findByIdAndStatus(Long id, RentalStatus status);
	RentalEntity findByAccountEntity_Id(Long accountId);
	RentalEntity findByVehicleEntity_Id(Long vehicleId);
	RentalEntity findByVehicleEntity_IdAndStatus(Long vehicleId, RentalStatus status);
	RentalEntity findByAccountEntity_IdAndStatus(Long accountId, RentalStatus status);

	List<RentalEntity> findAllByAccountEntity_Id(Long accountId);
	List<RentalEntity> findAllByVehicleEntity_Id(Long vehicleId);
	List<RentalEntity> findAllByStatus(RentalStatus status);

	@Query("SELECT r FROM RentalEntity r WHERE r.vehicleEntity.id = :vehicleId AND "
			+ "((:dateStart BETWEEN r.dateStart AND r.dateEnd) OR "
			+ "(:dateEnd BETWEEN r.dateStart AND r.dateEnd) OR "
			+ "(r.dateStart BETWEEN :dateStart AND :dateEnd) OR "
			+ "(r.dateEnd BETWEEN :dateStart AND :dateEnd))")
	List<RentalEntity> findOverlappingRentals(@Param("vehicleId") Long vehicleId,
	                                          @Param("dateStart") LocalDate dateStart,
	                                          @Param("dateEnd") LocalDate dateEnd);
}