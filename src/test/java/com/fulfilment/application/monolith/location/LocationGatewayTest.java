package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class LocationGatewayTest {

    @Inject
    LocationGateway gateway;

    @Test
    public void testResolveExisting() {
        Location loc = gateway.resolveByIdentifier("AMSTERDAM-001");
        assertNotNull(loc);
        assertEquals(5, loc.maxNumberOfWarehouses);
    }

    @Test
    public void testResolveNotFound() {
        assertThrows(RuntimeException.class, () -> gateway.resolveByIdentifier("GHOST-CITY"));
    }

    @Test
    public void testResolve() {
        assertNotNull(gateway.resolveByIdentifier("ZWOLLE-001"));
        assertThrows(RuntimeException.class, () -> gateway.resolveByIdentifier("NOT-HERE"));
    }
}