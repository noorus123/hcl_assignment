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
    public void testStoreCompleteWorkflow() {
        // 1. CREATE SUCCESS (Short name to avoid 40-char limit)
        String uniqueName = "S-" + java.util.UUID.randomUUID().toString().substring(0, 5);
        Store s = new Store(uniqueName);
        s.quantityProductsInStock = 10;

        Response res = given()
                .contentType("application/json")
                .body(s)
                .post("/store");

        res.then().statusCode(201);
        String id = res.jsonPath().get("id").toString();

        // 2. GET ALL & GET SINGLE
        given().get("/store").then().statusCode(200);
        given().get("/store/" + id).then().statusCode(200).body("name", is(uniqueName));

        // 3. PUT SUCCESS
        s.name = "Upd-" + uniqueName;
        given().contentType("application/json").body(s).put("/store/" + id).then().statusCode(200);

        // 4. PATCH SUCCESS (Hit name and stock branches)
        Store patch = new Store("Patch");
        patch.quantityProductsInStock = 50;
        given().contentType("application/json").body(patch).patch("/store/" + id).then().statusCode(200);

        // 5. Hit 'if (store.id != null)' in create
        Store sWithId = new Store("Fail");
        sWithId.id = 99L;
        given().contentType("application/json").body(sWithId).post("/store").then().statusCode(422);

        // 6. Hit 'if (name == null)' in update
        Store noName = new Store(null);
        given().contentType("application/json").body(noName).put("/store/" + id).then().statusCode(422);

        // 7. Hit 'entity == null' (Not Found)
        given().get("/store/9999").then().statusCode(404);
        given().delete("/store/9999").then().statusCode(404);
        given().contentType("application/json").body(s).put("/store/9999").then().statusCode(404);

        // 8. DELETE SUCCESS
        given().delete("/store/" + id).then().statusCode(204);
    }
}