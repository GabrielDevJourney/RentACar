package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.vehicle.VehicleDto;
import com.gabriel.rentacar.entity.VehicleEntity;
import com.gabriel.rentacar.enums.VehicleStatus;
import com.gabriel.rentacar.exception.vehicleException.*;
import com.gabriel.rentacar.mapper.VehicleMapper;
import com.gabriel.rentacar.repository.VehicleRepository;
import com.gabriel.rentacar.utils.PlateValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private PlateValidation plateValidator;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleDto vehicleDto;
    private VehicleEntity vehicleEntity;

    @BeforeEach
    void setUp() {
        vehicleDto = new VehicleDto();
        vehicleDto.setId(1L);
        vehicleDto.setPlate("AA-11-BB");
        vehicleDto.setBrand("Toyota");
        vehicleDto.setModel("Corolla");
        vehicleDto.setColor("Black");
        vehicleDto.setYearManufacture(2020);
        vehicleDto.setStatus(VehicleStatus.AVAILABLE);

        vehicleEntity = new VehicleEntity();
        vehicleEntity.setId(1L);
        vehicleEntity.setPlate("AA-11-BB");
        vehicleEntity.setBrand("TOYOTA");
        vehicleEntity.setModel("COROLLA");
        vehicleEntity.setColor("black");
        vehicleEntity.setYearManufacture(2020);
        vehicleEntity.setStatus(VehicleStatus.AVAILABLE);
    }

    @Test
    void createVehicle_Success() {
        when(plateValidator.validatePlateFormat(anyString(), anyInt())).thenReturn("AA-11-BB");
        when(vehicleRepository.existsByPlate(anyString())).thenReturn(false);
        when(vehicleMapper.toEntity(any(VehicleDto.class))).thenReturn(vehicleEntity);

        assertDoesNotThrow(() -> vehicleService.createVehicle(vehicleDto));

        verify(vehicleRepository).save(any(VehicleEntity.class));
    }

    @Test
    void createVehicle_WithExistingPlate_ThrowsException() {
        when(plateValidator.validatePlateFormat(anyString(), anyInt())).thenReturn("AA-11-BB");
        when(vehicleRepository.existsByPlate(anyString())).thenReturn(true);

        assertThrows(VehicleLicensePlateAlreadyExistsException.class,
                () -> vehicleService.createVehicle(vehicleDto));

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void createVehicle_WithInvalidYear_ThrowsException() {
        vehicleDto.setYearManufacture(Year.now().getValue() + 1);
        when(plateValidator.validatePlateFormat(anyString(), anyInt())).thenReturn("AA-11-BB");
        when(vehicleRepository.existsByPlate(anyString())).thenReturn(false);

        assertThrows(VehicleInvalidYearOfManufacture.class,
                () -> vehicleService.createVehicle(vehicleDto));
    }

    @Test
    void findByPlate_Success() {
        when(vehicleRepository.findByPlate(anyString())).thenReturn(Optional.of(vehicleEntity));
        when(vehicleMapper.toDto(any(VehicleEntity.class))).thenReturn(vehicleDto);

        VehicleDto result = vehicleService.findByPlate("AA-11-BB");

        assertNotNull(result);
        assertEquals("AA-11-BB", result.getPlate());
    }

    @Test
    void updateVehicleStatus_ToMaintenance_Success() {
        vehicleEntity.setStatus(VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(anyLong())).thenReturn(Optional.of(vehicleEntity));

        vehicleService.updateVehicleStatus(1L, VehicleStatus.MAINTENANCE);

        assertEquals(VehicleStatus.MAINTENANCE, vehicleEntity.getStatus());
        assertNotNull(vehicleEntity.getMaintenanceEndDate());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void updateVehicleStatus_InvalidTransition_ThrowsException() {
        vehicleEntity.setStatus(VehicleStatus.RENTED);
        when(vehicleRepository.findById(anyLong())).thenReturn(Optional.of(vehicleEntity));

        assertThrows(VehicleStatusRentedToDisableException.class,
                () -> vehicleService.updateVehicleStatus(1L, VehicleStatus.DISABLE));
    }

    @Test
    void completeRental_WithHighKilometers_SetToMaintenance() {
        vehicleEntity.setCurrentKilometers(1000);
        vehicleEntity.setMaintenanceKilometers(500);
        
        vehicleService.completeRental(vehicleEntity, 1000, 2000);

        assertEquals(VehicleStatus.MAINTENANCE, vehicleEntity.getStatus());
        assertEquals(2000, vehicleEntity.getCurrentKilometers());
        assertNotNull(vehicleEntity.getMaintenanceEndDate());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void completeRental_WithLowKilometers_SetToAvailable() {
        vehicleEntity.setCurrentKilometers(1000);
        vehicleEntity.setMaintenanceKilometers(1000);
        
        vehicleService.completeRental(vehicleEntity, 1000, 1500);

        assertEquals(VehicleStatus.AVAILABLE, vehicleEntity.getStatus());
        assertEquals(1500, vehicleEntity.getCurrentKilometers());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void getAllVehicles_Success() {
        List<VehicleEntity> vehicles = Arrays.asList(vehicleEntity);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(vehicleMapper.toDtoList(vehicles)).thenReturn(Arrays.asList(vehicleDto));

        List<VehicleDto> result = vehicleService.getAllVehicles();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getVehicleById_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleMapper.toDto(vehicleEntity)).thenReturn(vehicleDto);

        VehicleDto result = vehicleService.getVehicleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class,
                () -> vehicleService.getVehicleById(1L));
    }
}
