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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private VehicleEntity rentedVehicleEntity;
    private VehicleEntity maintenanceVehicleEntity;
    private VehicleEntity disabledVehicleEntity;

    @BeforeEach
    void setUp() {
        int currentYear = Year.now().getValue();

        // Setup VehicleDto for testing
        vehicleDto = new VehicleDto();
        vehicleDto.setPlate("AA-11-BB");
        vehicleDto.setBrand("Toyota");
        vehicleDto.setModel("Corolla");
        vehicleDto.setColor("Black");
        vehicleDto.setYearManufacture(currentYear);
        vehicleDto.setDailyRate(50.0);
        vehicleDto.setCurrentKilometers(1000);
        vehicleDto.setMaintenanceKilometers(5000);

        // Setup available VehicleEntity
        vehicleEntity = new VehicleEntity();
        vehicleEntity.setId(1L);
        vehicleEntity.setPlate("AA-11-BB");
        vehicleEntity.setBrand("TOYOTA");
        vehicleEntity.setModel("COROLLA");
        vehicleEntity.setColor("black");
        vehicleEntity.setYearManufacture(currentYear);
        vehicleEntity.setDailyRate(50.0);
        vehicleEntity.setStatus(VehicleStatus.AVAILABLE);
        vehicleEntity.setCurrentKilometers(1000);
        vehicleEntity.setMaintenanceKilometers(5000);

        // Setup rented VehicleEntity
        rentedVehicleEntity = new VehicleEntity();
        rentedVehicleEntity.setId(2L);
        rentedVehicleEntity.setPlate("BB-22-CC");
        rentedVehicleEntity.setBrand("HONDA");
        rentedVehicleEntity.setModel("CIVIC");
        rentedVehicleEntity.setColor("blue");
        rentedVehicleEntity.setYearManufacture(currentYear - 1);
        rentedVehicleEntity.setDailyRate(45.0);
        rentedVehicleEntity.setStatus(VehicleStatus.RENTED);
        rentedVehicleEntity.setCurrentKilometers(2000);
        rentedVehicleEntity.setMaintenanceKilometers(5000);

        // Setup maintenance VehicleEntity
        maintenanceVehicleEntity = new VehicleEntity();
        maintenanceVehicleEntity.setId(3L);
        maintenanceVehicleEntity.setPlate("CC-33-DD");
        maintenanceVehicleEntity.setBrand("NISSAN");
        maintenanceVehicleEntity.setModel("ALTIMA");
        maintenanceVehicleEntity.setColor("red");
        maintenanceVehicleEntity.setYearManufacture(currentYear - 2);
        maintenanceVehicleEntity.setDailyRate(40.0);
        maintenanceVehicleEntity.setStatus(VehicleStatus.MAINTENANCE);
        maintenanceVehicleEntity.setCurrentKilometers(3000);
        maintenanceVehicleEntity.setMaintenanceKilometers(5000);
        maintenanceVehicleEntity.setMaintenanceEndDate(LocalDate.now().plusDays(2));

        // Setup disabled VehicleEntity
        disabledVehicleEntity = new VehicleEntity();
        disabledVehicleEntity.setId(4L);
        disabledVehicleEntity.setPlate("DD-44-EE");
        disabledVehicleEntity.setBrand("FORD");
        disabledVehicleEntity.setModel("FOCUS");
        disabledVehicleEntity.setColor("white");
        disabledVehicleEntity.setYearManufacture(currentYear - 3);
        disabledVehicleEntity.setDailyRate(35.0);
        disabledVehicleEntity.setStatus(VehicleStatus.DISABLE);
        disabledVehicleEntity.setCurrentKilometers(4000);
        disabledVehicleEntity.setMaintenanceKilometers(5000);
    }

    // ========== CREATE VEHICLE TESTS ==========

    @Test
    void when_CreatingVehicle_then_Success() {
        // Setup
        when(plateValidator.validatePlateFormat(anyString(), anyInt())).thenReturn("AA-11-BB");
        when(vehicleRepository.existsByPlate(anyString())).thenReturn(false);
        when(vehicleMapper.toEntity(any(VehicleDto.class))).thenReturn(vehicleEntity);

        // Act & Assert
        assertDoesNotThrow(() -> vehicleService.createVehicle(vehicleDto));

        // Verify
        verify(vehicleRepository).save(any(VehicleEntity.class));
    }

    @Test
    void when_CreatingVehicle_with_ExistingPlate_then_ThrowsException() {
        // Setup
        when(plateValidator.validatePlateFormat(anyString(), anyInt())).thenReturn("AA-11-BB");
        when(vehicleRepository.existsByPlate(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(VehicleLicensePlateAlreadyExistsException.class,
                () -> vehicleService.createVehicle(vehicleDto));

        // Verify
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void when_CreatingVehicle_with_TooOldYear_then_ThrowsException() {
        // Setup
        int minYear = Year.now().getValue() - 20;
        vehicleDto.setYearManufacture(minYear - 1);

        // Act & Assert
        assertThrows(VehicleInvalidYearOfManufacture.class,
                () -> vehicleService.createVehicle(vehicleDto));
    }

    @Test
    void when_CreatingVehicle_with_FutureYear_then_ThrowsException() {
        // Setup
        int maxYear = Year.now().getValue();
        vehicleDto.setYearManufacture(maxYear + 1);

        // Act & Assert
        assertThrows(VehicleInvalidYearOfManufacture.class,
                () -> vehicleService.createVehicle(vehicleDto));
    }

    @Test
    void when_CreatingVehicle_with_NullVehicle_then_ThrowsException() {
        // Act & Assert
        assertThrows(VehicleInvalidDataException.class,
                () -> vehicleService.createVehicle(null));
    }

    // ========== FIND VEHICLE TESTS ==========

    @Test
    void when_FindingByPlate_then_Success() {
        // Setup
        when(vehicleRepository.findByPlate(anyString())).thenReturn(Optional.of(vehicleEntity));
        when(vehicleMapper.toDto(any(VehicleEntity.class))).thenReturn(vehicleDto);

        // Act
        VehicleDto result = vehicleService.findByPlate("AA-11-BB");

        // Assert
        assertNotNull(result);
        assertEquals("AA-11-BB", result.getPlate());
    }

    @Test
    void when_FindingByPlate_with_NonExistentPlate_then_ReturnsNull() {
        // Setup
        when(vehicleRepository.findByPlate(anyString())).thenReturn(Optional.empty());

        // Act
        VehicleDto result = vehicleService.findByPlate("XX-XX-XX");

        // Assert
        assertNull(result);
    }

    @Test
    void when_FindingByPlate_with_NullPlate_then_ThrowsException() {
        // Act & Assert
        assertThrows(VehicleInvalidDataException.class,
                () -> vehicleService.findByPlate(null));
    }

    @Test
    void when_GettingVehicleById_then_Success() {
        // Setup
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicleEntity));
        when(vehicleMapper.toDto(vehicleEntity)).thenReturn(vehicleDto);

        // Act
        VehicleDto result = vehicleService.getVehicleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("AA-11-BB", result.getPlate());
    }

    @Test
    void when_GettingVehicleById_with_NotFoundId_then_ThrowsException() {
        // Setup
        when(vehicleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(VehicleNotFoundException.class,
                () -> vehicleService.getVehicleById(1L));
    }

    @Test
    void when_GettingAllVehicles_then_Success() {
        // Setup
        List<VehicleEntity> vehicles = Arrays.asList(vehicleEntity, rentedVehicleEntity);
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(vehicleMapper.toDtoList(vehicles)).thenReturn(Arrays.asList(vehicleDto, vehicleDto));

        // Act
        List<VehicleDto> result = vehicleService.getAllVehicles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ========== UPDATE STATUS TESTS ==========

    @Test
    void when_UpdatingVehicleStatus_from_AvailableToMaintenance_then_Success() {
        // Setup
        when(vehicleRepository.findById(vehicleEntity.getId())).thenReturn(Optional.of(vehicleEntity));

        // Act
        vehicleService.updateVehicleStatus(vehicleEntity.getId(), VehicleStatus.MAINTENANCE);

        // Assert
        assertEquals(VehicleStatus.MAINTENANCE, vehicleEntity.getStatus());
        assertNotNull(vehicleEntity.getMaintenanceEndDate());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void when_UpdatingVehicleStatus_from_AvailableToRented_then_Success() {
        // Setup
        when(vehicleRepository.findById(vehicleEntity.getId())).thenReturn(Optional.of(vehicleEntity));

        // Act
        vehicleService.updateVehicleStatus(vehicleEntity.getId(), VehicleStatus.RENTED);

        // Assert
        assertEquals(VehicleStatus.RENTED, vehicleEntity.getStatus());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void when_UpdatingVehicleStatus_from_AvailableToDisable_then_Success() {
        // Setup
        when(vehicleRepository.findById(vehicleEntity.getId())).thenReturn(Optional.of(vehicleEntity));

        // Act
        vehicleService.updateVehicleStatus(vehicleEntity.getId(), VehicleStatus.DISABLE);

        // Assert
        assertEquals(VehicleStatus.DISABLE, vehicleEntity.getStatus());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void when_UpdatingVehicleStatus_from_RentedToDisable_then_ThrowsException() {
        // Setup
        when(vehicleRepository.findById(rentedVehicleEntity.getId())).thenReturn(Optional.of(rentedVehicleEntity));

        // Act & Assert
        assertThrows(VehicleStatusRentedToDisableException.class,
                () -> vehicleService.updateVehicleStatus(rentedVehicleEntity.getId(), VehicleStatus.DISABLE));
    }

    @Test
    void when_UpdatingVehicleStatus_from_DisableToMaintenance_then_ThrowsException() {
        // Setup
        when(vehicleRepository.findById(disabledVehicleEntity.getId())).thenReturn(Optional.of(disabledVehicleEntity));

        // Act & Assert
        assertThrows(VehicleStatusDisableToMaintenanceException.class,
                () -> vehicleService.updateVehicleStatus(disabledVehicleEntity.getId(), VehicleStatus.MAINTENANCE));
    }

    @Test
    void when_UpdatingVehicleStatus_from_MaintenanceToAvailable_then_Success() {
        // Setup
        when(vehicleRepository.findById(maintenanceVehicleEntity.getId())).thenReturn(Optional.of(maintenanceVehicleEntity));

        // Act
        vehicleService.updateVehicleStatus(maintenanceVehicleEntity.getId(), VehicleStatus.AVAILABLE);

        // Assert
        assertEquals(VehicleStatus.AVAILABLE, maintenanceVehicleEntity.getStatus());
        assertNull(null);
        verify(vehicleRepository).save(maintenanceVehicleEntity);
    }

    @Test
    void when_UpdatingVehicleStatus_with_NullVehicleId_then_ThrowsException() {
        // Act & Assert
        assertThrows(VehicleInvalidDataException.class,
                () -> vehicleService.updateVehicleStatus(null, VehicleStatus.AVAILABLE));
    }

    @Test
    void when_UpdatingVehicleStatus_with_NullStatus_then_ThrowsException() {
        // Act & Assert
        assertThrows(VehicleInvalidDataException.class,
                () -> vehicleService.updateVehicleStatus(1L, null));
    }

    // ========== COMPLETE RENTAL TESTS ==========

    @Test
    void when_CompletingRental_with_HighKilometers_then_SetToMaintenance() {
        // Setup
        vehicleEntity.setCurrentKilometers(1000);
        vehicleEntity.setMaintenanceKilometers(500);

        // Act
        vehicleService.completeRental(vehicleEntity, 1000, 2000);

        // Assert
        assertEquals(VehicleStatus.MAINTENANCE, vehicleEntity.getStatus());
        assertEquals(2000, vehicleEntity.getCurrentKilometers());
        assertNotNull(vehicleEntity.getMaintenanceEndDate());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void when_CompletingRental_with_LowKilometers_then_SetToAvailable() {
        // Setup
        vehicleEntity.setCurrentKilometers(1000);
        vehicleEntity.setMaintenanceKilometers(2000);

        // Act
        vehicleService.completeRental(vehicleEntity, 1000, 1500);

        // Assert
        assertEquals(VehicleStatus.AVAILABLE, vehicleEntity.getStatus());
        assertEquals(1500, vehicleEntity.getCurrentKilometers());
        verify(vehicleRepository).save(vehicleEntity);
    }

    @Test
    void when_CompletingRental_with_NullVehicle_then_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> vehicleService.completeRental(null, 1000, 1500));
    }

    // ========== SCHEDULED TASK TESTS ==========

    @Test
    void when_UpdatingMaintenanceVehicles_with_EndDateToday_then_Success() {
        // Setup
        List<VehicleEntity> maintenanceVehicles = Arrays.asList(maintenanceVehicleEntity);
        LocalDate today = LocalDate.now();
        maintenanceVehicleEntity.setMaintenanceEndDate(today);

        when(vehicleRepository.findAllByStatus(VehicleStatus.MAINTENANCE)).thenReturn(maintenanceVehicles);

        // Act
        vehicleService.updateMaintenanceVehicles();

        // Assert
        assertEquals(VehicleStatus.AVAILABLE, maintenanceVehicleEntity.getStatus());
        assertNull(maintenanceVehicleEntity.getMaintenanceEndDate());
        verify(vehicleRepository).save(maintenanceVehicleEntity);
    }

    @Test
    void when_UpdatingMaintenanceVehicles_with_FutureEndDate_then_NotUpdated() {
        // Setup
        List<VehicleEntity> maintenanceVehicles = Arrays.asList(maintenanceVehicleEntity);
        LocalDate future = LocalDate.now().plusDays(3);
        maintenanceVehicleEntity.setMaintenanceEndDate(future);

        when(vehicleRepository.findAllByStatus(VehicleStatus.MAINTENANCE)).thenReturn(maintenanceVehicles);

        // Act
        vehicleService.updateMaintenanceVehicles();

        // Assert
        assertEquals(VehicleStatus.MAINTENANCE, maintenanceVehicleEntity.getStatus());
        assertEquals(future, maintenanceVehicleEntity.getMaintenanceEndDate());
        verify(vehicleRepository, never()).save(maintenanceVehicleEntity);
    }
}