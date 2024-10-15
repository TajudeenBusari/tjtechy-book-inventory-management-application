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
* To launch application: java -jar target/tjtechy-inventory-management-sept2024-0.0.1-SNAPSHOT.jar
* To build docker images: docker build -t tcu/tjtechy-inventory-management-sept2024:1.0 .
* To instantiate the image: docker run -d -p 8081:8081 tcu/tjtechy-inventory-management-sept2024:1.0
* docker run -d -p 8081:8080 tcu/tjtechy-inventory-management-sept2024:1.0
 * To build new package: ./mvnw package -DskipTests
 * DROP TABLE IF EXISTS author_seq; works well to delete table from my work bench
 * but first delete books
 * Postgres supports UUID by default, so I have implemented this in my spring boot application
 * */