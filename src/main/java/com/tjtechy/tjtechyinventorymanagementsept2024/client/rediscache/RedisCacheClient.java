package com.tjtechy.tjtechyinventorymanagementsept2024.client.rediscache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheClient {
  private final StringRedisTemplate redisTemplate;

  public RedisCacheClient(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  //the set method stores a value in the cache with a timeout
  public void set(String key, String value, long timeout, TimeUnit timeUnit) {
    this.redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
  }

  //the get method retrieves a value from the cache
  public String get(String key) {
    return this.redisTemplate.opsForValue().get(key);
  }

  //the delete method removes a value from the cache
  public void delete(String key) {
    this.redisTemplate.delete(key);
  }

  //check if user exists in the whitelist
  public boolean isUserTokenInWhitelist(String userId, String tokenFromRequest) {
    String tokenFromRedis = get("whitelist:" + userId);
    return tokenFromRedis != null && tokenFromRedis.equals(tokenFromRequest);

  }
}
