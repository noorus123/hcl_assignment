package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

    private static final Logger LOG = Logger.getLogger(WarehouseRepository.class);

    @Override
    public List<Warehouse> getAll() {
        LOG.debug("Fetching all warehouses");
        return this.listAll().stream()
                .map(DbWarehouse::toWarehouse)
                .collect(Collectors.toList());
    }

    @Override
    public void create(Warehouse warehouse) {
        LOG.info("Persisting new warehouse: " + warehouse.businessUnitCode);
        DbWarehouse entity = new DbWarehouse();
        entity.businessUnitCode = warehouse.businessUnitCode;
        entity.location = warehouse.location;
        entity.capacity = warehouse.capacity;
        entity.stock = warehouse.stock;
        entity.createdAt = warehouse.createdAt;
        entity.archivedAt = warehouse.archivedAt;

        persist(entity);
    }

    @Override
    public void update(Warehouse warehouse) {
        LOG.info("Updating warehouse: " + warehouse.businessUnitCode);
        DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
        if (entity != null) {
            entity.location = warehouse.location;
            entity.capacity = warehouse.capacity;
            entity.stock = warehouse.stock;
            entity.createdAt = warehouse.createdAt;
            entity.archivedAt = warehouse.archivedAt;
        } else {
            throw new RuntimeException("Warehouse not found for update: " + warehouse.businessUnitCode);
        }
    }

    @Override
    public void remove(Warehouse warehouse) {
        LOG.info("Deleting warehouse: " + warehouse.businessUnitCode);
        delete("businessUnitCode", warehouse.businessUnitCode);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
        LOG.debug("Finding warehouse by BU code: " + buCode);
        DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
        if (entity == null) {
            return null;
        }
        return entity.toWarehouse();
    }
}