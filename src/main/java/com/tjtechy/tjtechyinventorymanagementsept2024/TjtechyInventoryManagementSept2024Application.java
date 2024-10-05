package com.tjtechy.tjtechyinventorymanagementsept2024;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TjtechyInventoryManagementSept2024Application {

    public static void main(String[] args) {
        SpringApplication.run(TjtechyInventoryManagementSept2024Application.class, args);
    }

}
/*TO MAKE REQUEST VIA TERMINAL
* C:\Users\tajud>curl http://localhost:8081/api/v1/users -H "Authorization: Bearer jwToken" -v
*
* */