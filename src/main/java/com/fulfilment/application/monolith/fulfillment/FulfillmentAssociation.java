package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfillment_association")
public class FulfillmentAssociation extends PanacheEntity {
    public Long storeId;
    public Long warehouseId;
    public Long productId;

    public FulfillmentAssociation() {}

    public FulfillmentAssociation(Long storeId, Long warehouseId, Long productId) {
        this.storeId = storeId;
        this.warehouseId = warehouseId;
        this.productId = productId;
    }
}