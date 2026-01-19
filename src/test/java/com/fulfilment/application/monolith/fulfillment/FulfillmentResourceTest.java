package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class FulfillmentResourceTest {

    @Test
    public void testFulfillmentConstraints() {

        // --- 1. Test Constraint: Max 2 Warehouses per Product per Store ---
        associate(10L, 101L, 10L, 201);
        associate(10L, 102L, 10L, 201);
        associate(10L, 103L, 10L, 400);

        // --- 2. Test Constraint: Max 3 Warehouses per Store ---
        associate(20L, 201L, 1L, 201);
        associate(20L, 202L, 2L, 201);
        associate(20L, 203L, 3L, 201);
        associate(20L, 204L, 4L, 400);

        // --- 3. Test Constraint: Max 5 Product Types per Warehouse ---
        for (long pId = 1; pId <= 5; pId++) {
            associate(30L, 50L, pId, 201);
        }
        // Adding the 6th product type to Warehouse 50
        associate(30L, 50L, 6L, 400);
    }

    private void associate(Long storeId, Long warehouseId, Long productId, int expectedStatus) {
        FulfillmentAssociation req = new FulfillmentAssociation(storeId, warehouseId, productId);

        given()
                .contentType("application/json")
                .body(req)
                .when()
                .post("/fulfillment")
                .then()
                .statusCode(expectedStatus);
    }
}