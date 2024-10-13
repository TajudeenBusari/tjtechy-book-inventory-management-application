package com.tjtechy.tjtechyinventorymanagementsept2024;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
/**
 * this will override the active profile in application.yml file.
 * Irrespective of the active profile, the test will only run using the
 * h2-database
 *
 */
@ActiveProfiles(value = "h2-database")
class TjtechyInventoryManagementSept2024ApplicationTests {

    @Test
    void contextLoads() {
    }

}
