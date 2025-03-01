package com.gabriel.rentacar.aspects;

import com.gabriel.rentacar.dto.vehicle.VehicleDto;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class VehicleAspect {
	private static final Logger logger = LoggerFactory.getLogger(VehicleAspect.class);

	@Before("execution(* com.gabriel.rentacar.service.VehicleService.createVehicle(..)) && args(vehicleDto)")
	public void logBeforeCreateVehicle(VehicleDto vehicleDto) {
		logger.info("Creating vehicle with Plate: {}, Brand: {}, Model: {}, Color: {}, Year Manufacture: {}, Daily " +
						"Rate : {} ",
				vehicleDto.getPlate(),
				vehicleDto.getBrand(),
				vehicleDto.getModel(),
				vehicleDto.getColor(),
				vehicleDto.getYearManufacture(),
				vehicleDto.getDailyRate());
	}

	@AfterReturning("execution(* com.gabriel.rentacar.service.VehicleService.createVehicle(..))")
	public void logAfterCreateVehicle() {
		logger.info("Vehicle created with success!");
	}
}
