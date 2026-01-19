package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProductEndpointTest {

    @Test
    public void testProductManagementLifecycle() {
        // 1. Create
        Product p = new Product("P-" + java.util.UUID.randomUUID().toString().substring(0, 5));
        p.stock = 20;
        Response res = given().contentType("application/json").body(p).post("/product");
        String id = res.jsonPath().get("id").toString();

        // 2. Update Success
        p.name = "UpdatedProd";
        given().contentType("application/json").body(p).put("/product/" + id).then().statusCode(200);

        // 3. GET success
        given().get("/product/" + id).then().statusCode(200);

        // 4. Error Paths
        given().get("/product/9999").then().statusCode(404);
        given().delete("/product/9999").then().statusCode(404);

        // 5. Validation Error
        p.id = null;
        p.name = null;
        given().contentType("application/json").body(p).put("/product/" + id).then().statusCode(422);

        // 6. Final Delete
        given().delete("/product/" + id).then().statusCode(204);
    }
}