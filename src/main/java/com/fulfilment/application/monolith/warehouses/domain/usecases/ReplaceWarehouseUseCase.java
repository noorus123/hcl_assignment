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

    public ReplaceWarehouseUseCase(WarehouseStore warehouseStore,
                                   ArchiveWarehouseUseCase archiveWarehouseUseCase,
                                   CreateWarehouseUseCase createWarehouseUseCase) {
        this.warehouseStore = warehouseStore;
        this.archiveWarehouseUseCase = archiveWarehouseUseCase;
        this.createWarehouseUseCase = createWarehouseUseCase;
    }

    @Override
    @Transactional
    public void replace(String oldBusinessUnitCode, Warehouse newWarehouse) {

        // 1. Find the Old Warehouse
        Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(oldBusinessUnitCode);
        if (oldWarehouse == null) {
            throw new IllegalArgumentException("Warehouse to replace not found: " + oldBusinessUnitCode);
        }

        // 2. Validate: Capacity Accommodation
        if (newWarehouse.capacity < oldWarehouse.stock) {
            throw new IllegalArgumentException("New warehouse capacity (" + newWarehouse.capacity
                    + ") is too small for existing stock (" + oldWarehouse.stock + ")");
        }

        // 3. Validate: Stock Matching
        if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
            throw new IllegalArgumentException("New warehouse stock must match the old warehouse stock during replacement.");
        }

        // 4. Archive the Old Warehouse
        archiveWarehouseUseCase.archive(oldWarehouse);

        // 5. Create the New Warehouse
        createWarehouseUseCase.create(newWarehouse);
    }
}