package com.fulfilment.application.monolith.fulfillment;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/fulfillment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

    @Inject AssociateWarehouseUseCase useCase;

    @POST
    public Response associate(FulfillmentAssociation request) {
        try {
            useCase.associate(request.storeId, request.warehouseId, request.productId);
            return Response.status(201).build();
        } catch (IllegalStateException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}