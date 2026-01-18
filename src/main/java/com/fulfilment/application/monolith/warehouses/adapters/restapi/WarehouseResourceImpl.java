package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject WarehouseRepository warehouseRepository; // Keep for Reads (CQRS style)
    @Inject CreateWarehouseUseCase createWarehouseUseCase;
    @Inject ArchiveWarehouseUseCase archiveWarehouseUseCase;
    @Inject ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        // Read directly from Repo (Fast)
        return warehouseRepository.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        try {
            var domainWarehouse = toDomain(data);
            createWarehouseUseCase.create(domainWarehouse);
            return toResponse(domainWarehouse);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Convert Java Error -> HTTP 400 Bad Request
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {
        // Note: The API 'id' maps to our 'BusinessUnitCode'
        var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);
        if (domainWarehouse == null) {
            throw new WebApplicationException("Warehouse not found", Response.Status.NOT_FOUND);
        }
        return toResponse(domainWarehouse);
    }

    @Override
    public void archiveAWarehouseUnitByID(String id) {
        var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);
        if (domainWarehouse == null) {
            throw new WebApplicationException("Warehouse not found", Response.Status.NOT_FOUND);
        }

        archiveWarehouseUseCase.archive(domainWarehouse);
    }

    @Override
    public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
        try {
            var domainNewWarehouse = toDomain(data);

            // Call the Replace Logic (Validation + Archive + Create)
            replaceWarehouseUseCase.replace(businessUnitCode, domainNewWarehouse);

            return toResponse(domainNewWarehouse);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    // --- Mappers (Helper Methods) ---

    private Warehouse toResponse(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain) {
        var response = new Warehouse();
        response.setBusinessUnitCode(domain.businessUnitCode);
        response.setLocation(domain.location);
        response.setCapacity(domain.capacity);
        response.setStock(domain.stock);
        return response;
    }

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(Warehouse apiBean) {
        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = apiBean.getBusinessUnitCode();
        domain.location = apiBean.getLocation();
        domain.capacity = apiBean.getCapacity();
        domain.stock = apiBean.getStock();
        return domain;
    }
}