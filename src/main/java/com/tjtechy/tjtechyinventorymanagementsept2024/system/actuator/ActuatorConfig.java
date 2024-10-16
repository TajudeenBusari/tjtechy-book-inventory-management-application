package com.tjtechy.tjtechyinventorymanagementsept2024.system.actuator;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

  @Bean
  public HttpExchangeRepository httpExchangeRepository(){
    var repo = new InMemoryHttpExchangeRepository();
    //repo.setCapacity(1000);//monitors request response exchanges, set to 1000
    return repo;
  }
  /***
   * this makes the actuator/httpexchanges to work
   */
}
