package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
@Transactional
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject WarehouseRepository warehouseRepository;
    @Inject CreateWarehouseUseCase createWarehouseUseCase;
    @Inject ArchiveWarehouseUseCase archiveWarehouseUseCase;
    @Inject ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll().stream().map(this::toResponse).toList();
    }

    @Override
    public Warehouse createANewWarehouseUnit(Warehouse data) {
        try {
            var domain = toDomain(data);
            createWarehouseUseCase.create(domain);
            return toResponse(domain);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Map business rule violations to 400 Bad Request
            throw new WebApplicationException(e.getMessage(), 400);
        }
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {
        var domain = warehouseRepository.findByBusinessUnitCode(id);
        if (domain == null) throw new WebApplicationException("Warehouse not found", 404);
        return toResponse(domain);
    }

    @Override
    public void archiveAWarehouseUnitByID(String id) {
        var domain = warehouseRepository.findByBusinessUnitCode(id);
        if (domain == null) throw new WebApplicationException("Warehouse not found", 404);
        archiveWarehouseUseCase.archive(domain);
    }

    @Override
    public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, Warehouse data) {
        try {
            replaceWarehouseUseCase.replace(businessUnitCode, toDomain(data));
            return data;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new WebApplicationException(e.getMessage(), 400);
        }
    }

    // Mapper: Domain Model -> API Bean
    private Warehouse toResponse(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain) {
        var r = new Warehouse();
        r.setBusinessUnitCode(domain.businessUnitCode);
        r.setLocation(domain.location);
        r.setCapacity(domain.capacity);
        r.setStock(domain.stock);
        return r;
    }

    // Mapper: API Bean -> Domain Model
    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(Warehouse api) {
        var d = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        d.businessUnitCode = api.getBusinessUnitCode();
        d.location = api.getLocation();
        d.capacity = api.getCapacity();
        d.stock = api.getStock();
        return d;
    }
}