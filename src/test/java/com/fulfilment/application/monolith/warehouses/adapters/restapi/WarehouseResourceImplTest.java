package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class WarehouseResourceImplTest {

    @Test
    public void testWarehouseLifecycle() {
        // Unique code
        String code = "W-" + java.util.UUID.randomUUID().toString().substring(0, 5);
        Warehouse wh = new Warehouse();
        wh.setBusinessUnitCode(code);
        wh.setLocation("AMSTERDAM-002");
        wh.setCapacity(20);
        wh.setStock(5);

        // 1. Create
        given().contentType("application/json").body(wh).post("/warehouse").then().statusCode(200);

        // 2. Replace
        wh.setCapacity(30);
        given().contentType("application/json").body(wh).post("/warehouse/" + code + "/replacement").then().statusCode(200);

        // 3. Get, List, and Delete
        given().get("/warehouse/" + code).then().statusCode(200).body("capacity", is(30));
        given().get("/warehouse").then().statusCode(200);
        given().delete("/warehouse/" + code).then().statusCode(204);
    }
}