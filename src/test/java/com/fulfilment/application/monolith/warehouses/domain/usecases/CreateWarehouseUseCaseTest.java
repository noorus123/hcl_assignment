package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    @Mock
    LocationResolver locationResolver;

    @InjectMocks
    CreateWarehouseUseCase createWarehouseUseCase;

    private Warehouse validWarehouse;
    private Location validLocation;

    @BeforeEach
    void setUp() {
        // Setup standard valid data for happy path
        validWarehouse = new Warehouse();
        validWarehouse.businessUnitCode = "MWH.999";
        validWarehouse.location = "ZWOLLE-001";
        validWarehouse.capacity = 100;
        validWarehouse.stock = 50;

        validLocation = new Location("ZWOLLE-001", 5, 500);
    }

    @Test
    void testCreate_Success() {
        // Given
        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null); // Unique ID
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList()); // No existing warehouses

        // When
        createWarehouseUseCase.create(validWarehouse);

        // Then
        verify(warehouseStore, times(1)).create(validWarehouse);
        assertNotNull(validWarehouse.createdAt);
    }

    @Test
    void testCreate_Fail_DuplicateBusinessUnitCode() {
        // Given
        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(new Warehouse());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createWarehouseUseCase.create(validWarehouse);
        });

        assertEquals("Warehouse with Business Unit Code MWH.999 already exists.", exception.getMessage());
    }

    @Test
    void testCreate_Fail_StockExceedsCapacity() {
        // Given
        validWarehouse.stock = 150; // Stock > Capacity (100)

        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            createWarehouseUseCase.create(validWarehouse);
        });

        assertEquals("Stock cannot exceed warehouse capacity.", exception.getMessage());
    }

    @Test
    void testCreate_Fail_LocationMaxWarehousesReached() {
        // Given
        validLocation.maxNumberOfWarehouses = 1; // Only 1 allowed

        // Simulate 1 existing warehouse in the same location
        Warehouse existing = new Warehouse();
        existing.location = "ZWOLLE-001";
        when(warehouseStore.getAll()).thenReturn(List.of(existing));

        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            createWarehouseUseCase.create(validWarehouse);
        });

        assertTrue(exception.getMessage().contains("Max number of warehouses"));
    }

    @Test
    void testCreate_Fail_LocationMaxCapacityReached() {
        // Given
        validLocation.maxCapacity = 150;

        // Simulate existing warehouse taking up 100 capacity
        Warehouse existing = new Warehouse();
        existing.location = "ZWOLLE-001";
        existing.capacity = 100;

        // New warehouse wants 100 capacity. 100 + 100 = 200 > 150. FAIL.
        when(warehouseStore.getAll()).thenReturn(List.of(existing));
        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            createWarehouseUseCase.create(validWarehouse);
        });

        assertTrue(exception.getMessage().contains("Max capacity"));
    }

    @Test
    void testCreate_Fail_MaxCountReached() {
        validLocation.maxNumberOfWarehouses = 0; // Location is full
        when(locationResolver.resolveByIdentifier(anyString())).thenReturn(validLocation);
        when(warehouseStore.getAll()).thenReturn(java.util.Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(validWarehouse));
    }

    @Test
    void testCreate_Fail_MaxCapacityExceeded() {
        validLocation.maxCapacity = 10;
        validWarehouse.capacity = 50; // 50 > 10
        when(locationResolver.resolveByIdentifier(anyString())).thenReturn(validLocation);

        assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(validWarehouse));
    }

    @Test
    void testCreate_Fail_CapacityExceeded() {
        validLocation.maxCapacity = 10;
        validWarehouse.capacity = 50; // 50 > 10
        when(locationResolver.resolveByIdentifier(anyString())).thenReturn(validLocation);
        when(warehouseStore.getAll()).thenReturn(java.util.Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(validWarehouse));
    }

    @Test
    void testCreate_Fail_LocationFull() {
        validLocation.maxNumberOfWarehouses = 1;
        Warehouse existing = new Warehouse();
        existing.location = "ZWOLLE-001";

        when(locationResolver.resolveByIdentifier(anyString())).thenReturn(validLocation);
        when(warehouseStore.getAll()).thenReturn(java.util.List.of(existing));

        assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(validWarehouse));
    }
}