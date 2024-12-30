package com.tjtechy.tjtechyinventorymanagementsept2024.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component //because we have to inject in the security configuration class
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

  //THE URI_TEMPLATE IS USED TO EXTRACT AND MATCH THE USERID FROM THE REQUEST URI.
  private static final UriTemplate USER_URI_TEMPLATE = new UriTemplate("/users/{userId}");

  /**
   * Deprecated method implementation to satisfy interface requirements.
   * This method is deprecated and may be removed in the future.
   * It is recommended to use the authorize method instead,
   * and it is just here to satisfy the interface requirements.
   * I am not using the method for any purpose.
   */
  @Override
  @Deprecated
  public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
    return this.authorize(authentication, object);
  }

  @Override

  public AuthorizationDecision authorize(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {

    //Extract the userId from the request URI( URI: /users/{userId}).
    Map<String, String> uriVariables = USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
    var userIdFromUri = uriVariables.get("userId");

    //Extract the userId from the Authentication object, which is the Jwt object.
    Authentication authentication = authenticationSupplier.get();
    var userIdFromJwt = ((Jwt) authentication.getPrincipal()).getClaim("userId").toString();


    //Check if user has the role "ROLE_user".
    boolean hasUserRole = authentication.getAuthorities()
            .stream()
            .anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_user"));

    //Check if user has the role "ROLE_Admin".
    boolean hasAdminRole = authentication.getAuthorities()
            .stream()
            .anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_Admin"));

    //Compare the userId's and if same return AuthorizationDecision.ALLOWED else return AuthorizationDecision.DENIED.

    boolean userIdMatch = userIdFromUri != null && userIdFromUri.equals(userIdFromJwt);

    return new AuthorizationDecision(hasAdminRole || (hasUserRole && userIdMatch));

  }
}

/***
 * The check method is deprecated, and the alternative method is authorized.
 * You can replace the usage of check with authorize in your code.
 * Make the whole class abstract and implement the authorize method because the check method
 * is deprecated but not removed yet from the AuthorizationManager interface.
 * In userIdFromUri and userIdFromJwt, we are trying to match the id from request uri and the id from the jwt token.
 */