package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {

    private static final Logger LOG = Logger.getLogger(LegacyStoreManagerGateway.class);

    public void createStoreOnLegacySystem(Store store) {
        LOG.info("Sending CREATE store request to legacy system: " + store.name);
        writeToFile(store);
    }

    public void updateStoreOnLegacySystem(Store store) {
        LOG.info("Sending UPDATE store request to legacy system: " + store.name);
        writeToFile(store);
    }

    private void writeToFile(Store store) {
        try {
            // Step 1: Create a temporary file
            Path tempFile;

            tempFile = Files.createTempFile(store.name, ".txt");
            LOG.info("Temporary file created at: " + tempFile);

            // Step 2: Write data to the temporary file
            String content =
                    "Store created. [ name ="
                            + store.name
                            + " ] [ items on stock ="
                            + store.quantityProductsInStock
                            + "]";
            Files.write(tempFile, content.getBytes());
            LOG.debug("Data written to temporary file");

            // Step 3: Optionally, read the data back to verify
            String readContent = new String(Files.readAllBytes(tempFile));
            LOG.debug("Data read from temporary file: " + readContent);

            // Step 4: Delete the temporary file when done
            Files.delete(tempFile);
            LOG.debug("Temporary file deleted");

        } catch (Exception e) {
            LOG.error("Error while writing to legacy system file", e);
        }
    }
}
