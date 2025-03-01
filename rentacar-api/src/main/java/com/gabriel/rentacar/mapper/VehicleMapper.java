package com.gabriel.rentacar.mapper;

import com.gabriel.rentacar.dto.vehicle.VehicleDto;
import com.gabriel.rentacar.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
	VehicleDto toDto(VehicleEntity entity);

	@Mapping(target = "id", ignore = true)
	VehicleEntity toEntity(VehicleDto dto);

	List<VehicleDto> toDtoList(List<VehicleEntity> entities);
}