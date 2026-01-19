package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ReplaceWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    @Mock
    ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @Mock
    CreateWarehouseUseCase createWarehouseUseCase;

    @InjectMocks
    ReplaceWarehouseUseCase replaceWarehouseUseCase;

    private Warehouse oldWarehouse;
    private Warehouse newWarehouse;

    @BeforeEach
    void setUp() {
        oldWarehouse = new Warehouse();
        oldWarehouse.businessUnitCode = "OLD.001";
        oldWarehouse.capacity = 100;
        oldWarehouse.stock = 50;

        newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "NEW.001";
        newWarehouse.capacity = 100; // Same capacity
        newWarehouse.stock = 50;     // Same stock
    }

    @Test
    void testReplace_Success() {
        // Given
        when(warehouseStore.findByBusinessUnitCode("OLD.001")).thenReturn(oldWarehouse);

        // When
        replaceWarehouseUseCase.replace("OLD.001", newWarehouse);

        // Then
        // 1. Verify old is archived
        verify(archiveWarehouseUseCase, times(1)).archive(oldWarehouse);
        // 2. Verify new is created
        verify(createWarehouseUseCase, times(1)).create(newWarehouse);
    }

    @Test
    void testReplace_Fail_OldWarehouseNotFound() {
        // Given
        when(warehouseStore.findByBusinessUnitCode("MISSING.001")).thenReturn(null);

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace("MISSING.001", newWarehouse);
        });

        assertEquals("Warehouse to replace not found: MISSING.001", ex.getMessage());
        verify(archiveWarehouseUseCase, never()).archive(any());
    }

    @Test
    void testReplace_Fail_CapacityTooSmall() {
        // Given
        oldWarehouse.stock = 80;
        newWarehouse.capacity = 50; // New capacity (50) < Old Stock (80)

        when(warehouseStore.findByBusinessUnitCode("OLD.001")).thenReturn(oldWarehouse);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace("OLD.001", newWarehouse);
        });
    }

    @Test
    void testReplace_Fail_StockMismatch() {
        // Given
        oldWarehouse.stock = 50;
        newWarehouse.stock = 20; // Stock must match exactly during swap

        when(warehouseStore.findByBusinessUnitCode("OLD.001")).thenReturn(oldWarehouse);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace("OLD.001", newWarehouse);
        });
    }
}