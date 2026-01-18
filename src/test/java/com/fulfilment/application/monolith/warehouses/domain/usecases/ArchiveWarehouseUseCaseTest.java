package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArchiveWarehouseUseCaseTest {

    @Mock
    WarehouseStore warehouseStore;

    @InjectMocks
    ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @Test
    void testArchive_Success() {
        // Given
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.archivedAt = null;

        // When
        archiveWarehouseUseCase.archive(warehouse);

        // Then
        assertNotNull(warehouse.archivedAt, "ArchivedAt timestamp should be set");
        verify(warehouseStore, times(1)).update(warehouse);
    }

    @Test
    void testArchive_Null_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            archiveWarehouseUseCase.archive(null);
        });

        verify(warehouseStore, never()).update(any());
    }
}