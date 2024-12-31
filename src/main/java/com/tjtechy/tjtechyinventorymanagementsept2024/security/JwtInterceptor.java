package com.tjtechy.tjtechyinventorymanagementsept2024.security;

import com.tjtechy.tjtechyinventorymanagementsept2024.client.rediscache.RedisCacheClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

  private final RedisCacheClient redisCacheClient;

  public JwtInterceptor(RedisCacheClient redisCacheClient) {
    this.redisCacheClient = redisCacheClient;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    //get the token from request header
    String authorizationHeader = request.getHeader("Authorization");

    //if the token is not null and starts with "Bearer ", then we need to verify if this token is present in Redis
    //Else, this is just a public request that does not need a token E.g., login, register, etc
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Jwt jwt = (Jwt) authentication.getPrincipal();

      //Retrieve the userId from the JWT claims and check if the token is in the Redis white list or not
      String userId = jwt.getClaim("userId").toString();
      if(!this.redisCacheClient.isUserTokenInWhitelist(userId, jwt.getTokenValue())){
        throw new BadCredentialsException("Invalid token");
      }

    }
    return true;
  }


}


/**This class will let us confirm if token is present in redis or not by interception
 * An interceptor intercepts and processes incoming HTTP request and
 * outgoing responses at a global level before they reach the controller
 * or after they leave the controller.
 * Register this class with spring boot (webConfig)
 * Spring security is invoked before this class (interceptor)
 */