package com.tjtechy.tjtechyinventorymanagementsept2024.system.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UsableMemoryHealthIndicator implements HealthIndicator {
  /**
   * @return
   * . is current directory. path used to compute available disk space
   */

  @Override
  public Health health() {
    File path = new File(".");
    long diskUsableinBytes = path.getUsableSpace();
    boolean isHealth = diskUsableinBytes >= 10 * 1024 * 1024; //10MB
    Status status = isHealth ? Status.UP : Status.DOWN; //UP means there is enough memory space

    return Health
            .status(status)
            .withDetail("usable memory", diskUsableinBytes)
            .withDetail("threshold", 10 * 1024 * 1024)
            .build();
  }
}
