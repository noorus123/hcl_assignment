package com.fulfilment.application.monolith.stores;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class StoreResourceTest {

    @InjectMock
    LegacyStoreManagerGateway legacyGateway;

    @Test
    public void testStoreFinalCoveragePush() {
        // --- 1. SETUP UNIQUE DATA ---
        String baseName = "S" + java.util.UUID.randomUUID().toString().substring(0, 5);
        Store s = new Store(baseName);
        s.quantityProductsInStock = 10;

        // --- 2. CREATE (POST) ---
        Response res = given().contentType("application/json").body(s).post("/store");
        String id = res.jsonPath().get("id").toString();

        // --- 3. GET ALL & SINGLE ---
        given().get("/store").then().statusCode(200);
        given().get("/store/" + id).then().statusCode(200).body("name", is(baseName));

        // --- 4. UPDATE (PUT) ---
        s.name = "U-" + baseName;
        given().contentType("application/json").body(s).put("/store/" + id).then().statusCode(200);

        // --- 5. PATCH BRANCHES  ---
        Store pName = new Store("P-" + baseName);
        given().contentType("application/json").body(pName).patch("/store/" + id).then().statusCode(200);

        // Patch only quantity
        Store pQty = new Store("P-" + baseName);
        pQty.quantityProductsInStock = 88;
        given().contentType("application/json").body(pQty).patch("/store/" + id).then().statusCode(200);

        // --- 6. ERROR BRANCHES  ---

        // A. Trigger ErrorMapper
        given().get("/store/99999").then().statusCode(404).body("code", is(404));

        // B. Trigger Validation Exception (Hits 'if name == null' branch)
        Store invalid = new Store(null);
        given().contentType("application/json").body(invalid).put("/store/" + id).then().statusCode(422);
        given().contentType("application/json").body(invalid).patch("/store/" + id).then().statusCode(422);

        // C. POST with ID already set
        Store idStore = new Store("Fail");
        idStore.id = 55L;
        given().contentType("application/json").body(idStore).post("/store").then().statusCode(422);

        // --- 7. DELETE ---
        given().delete("/store/" + id).then().statusCode(204);
        given().delete("/store/99999").then().statusCode(404);
    }
}