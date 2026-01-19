package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AssociateWarehouseUseCase {

    private static final Logger LOG = Logger.getLogger(AssociateWarehouseUseCase.class);

    @Transactional
    public void associate(Long storeId, Long warehouseId, Long productId) {
        LOG.info("Attempting to associate Warehouse " + warehouseId + " with Product " + productId + " for Store " + storeId);

        // Constraint 1: Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
        long warehouseCountForProduct = FulfillmentAssociation.count("productId = ?1 and storeId = ?2", productId, storeId);
        if (warehouseCountForProduct >= 2) {
            LOG.warn("Constraint Violated: Product already has 2 warehouses for this store");
            throw new IllegalStateException("Maximum 2 warehouses allowed per product per store.");
        }

        // Constraint 2: Each Store can be fulfilled by a maximum of 3 different Warehouses
        long uniqueWarehouseCountForStore = FulfillmentAssociation.find("storeId = ?1", storeId)
                .stream().map(f -> ((FulfillmentAssociation)f).warehouseId).distinct().count();
        if (uniqueWarehouseCountForStore >= 3) {
            LOG.warn("Constraint Violated: Store already has 3 warehouses");
            throw new IllegalStateException("Maximum 3 warehouses allowed per store.");
        }

        // Constraint 3: Each Warehouse can store maximally 5 types of Products
        long productTypeCount = FulfillmentAssociation.count("warehouseId = ?1", warehouseId);
        if (productTypeCount >= 5) {
            LOG.warn("Constraint Violated: Warehouse reached max product types");
            throw new IllegalStateException("Maximum 5 product types allowed per warehouse.");
        }

        new FulfillmentAssociation(storeId, warehouseId, productId).persist();
        LOG.info("Association successful");
    }
}