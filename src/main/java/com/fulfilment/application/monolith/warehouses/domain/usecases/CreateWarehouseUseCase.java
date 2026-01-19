package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

    private static final Logger LOG = Logger.getLogger(CreateWarehouseUseCase.class);
    private final WarehouseStore warehouseStore;
    private final LocationResolver locationResolver;

    public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
        this.warehouseStore = warehouseStore;
        this.locationResolver = locationResolver;
    }

    @Override
    public void create(Warehouse warehouse) {
        LOG.info("Creating warehouse with BU code: " + warehouse.businessUnitCode);

        // 1. Validate: Only block if an ACTIVE warehouse with this code exists
        if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
            LOG.warn("Duplicate warehouse BU code detected: " + warehouse.businessUnitCode);
            throw new IllegalArgumentException("Warehouse with Business Unit Code " + warehouse.businessUnitCode + " already exists.");
        }

        // 2. Validate: Location existence
        Location location = locationResolver.resolveByIdentifier(warehouse.location);

        // 3. Validate: Stock cannot exceed Capacity
        if (warehouse.stock > warehouse.capacity) {
            LOG.warn("Stock exceeds capacity for warehouse: " + warehouse.businessUnitCode);
            throw new IllegalArgumentException("Stock cannot exceed warehouse capacity.");
        }

        // 4. Validate: Feasibility & Capacity
        List<Warehouse> existingWarehousesInLocation = warehouseStore.getAll().stream()
                .filter(w -> Objects.equals(w.location, warehouse.location))
                .filter(w -> w.archivedAt == null) // Only count active warehouses
                .toList();

        // 4a. Check Max Number of Warehouses
        if (existingWarehousesInLocation.size() >= location.maxNumberOfWarehouses) {
            LOG.warn("Max warehouse count reached for location: " + warehouse.location);
            throw new IllegalStateException("Cannot create warehouse. Max number of warehouses ("
                    + location.maxNumberOfWarehouses + ") reached for location " + warehouse.location);
        }

        // 4b. Check Max Capacity
        int currentUsedCapacity = existingWarehousesInLocation.stream()
                .mapToInt(w -> w.capacity)
                .sum();

        if (currentUsedCapacity + warehouse.capacity > location.maxCapacity) {
            LOG.warn("Max capacity exceeded for location: " + warehouse.location);
            throw new IllegalStateException("Cannot create warehouse. Max capacity ("
                    + location.maxCapacity + ") would be exceeded for location " + warehouse.location);
        }

        // 5. Setup data and Save
        warehouse.createdAt = LocalDateTime.now();
        warehouse.archivedAt = null;

        warehouseStore.create(warehouse);
        LOG.info("Warehouse created successfully: " + warehouse.businessUnitCode);
    }
}