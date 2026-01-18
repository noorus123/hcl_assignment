package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

    private final WarehouseStore warehouseStore;
    private final ArchiveWarehouseUseCase archiveWarehouseUseCase;
    private final CreateWarehouseUseCase createWarehouseUseCase;

    // We inject the other UseCases to reuse their logic (DRY Principle)
    public ReplaceWarehouseUseCase(WarehouseStore warehouseStore,
                                   ArchiveWarehouseUseCase archiveWarehouseUseCase,
                                   CreateWarehouseUseCase createWarehouseUseCase) {
        this.warehouseStore = warehouseStore;
        this.archiveWarehouseUseCase = archiveWarehouseUseCase;
        this.createWarehouseUseCase = createWarehouseUseCase;
    }

    @Override
    @Transactional // Ensure the swap happens atomically
    public void replace(String oldBusinessUnitCode, Warehouse newWarehouse) {

        // 1. Find the Old Warehouse
        Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(oldBusinessUnitCode);
        if (oldWarehouse == null) {
            throw new IllegalArgumentException("Warehouse to replace not found: " + oldBusinessUnitCode);
        }

        // 2. Validate: Capacity Accommodation
        // "Ensure the new warehouse's capacity can accommodate the stock from the warehouse being replaced."
        if (newWarehouse.capacity < oldWarehouse.stock) {
            throw new IllegalArgumentException("New warehouse capacity (" + newWarehouse.capacity
                    + ") is too small for existing stock (" + oldWarehouse.stock + ")");
        }

        // 3. Validate: Stock Matching
        // "Confirm that the stock of the new warehouse matches the stock of the previous warehouse."
        // (Assuming we move the stock over exactly)
        if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
            throw new IllegalArgumentException("New warehouse stock must match the old warehouse stock during replacement.");
        }

        // 4. Archive the Old Warehouse
        // This frees up the "Location Count" limit logic in CreateUseCase
        archiveWarehouseUseCase.archive(oldWarehouse);

        // 5. Create the New Warehouse
        // This runs all the location validity checks defined in CreateUseCase
        createWarehouseUseCase.create(newWarehouse);
    }
}