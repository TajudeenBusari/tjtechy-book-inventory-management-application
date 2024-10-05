package com.tjtechy.tjtechyinventorymanagementsept2024.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class SecurityConfiguration {

    private final RSAPublicKey publicKey;

    private final RSAPrivateKey privateKey;


    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private final CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    private final CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;

    private final CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;

        public SecurityConfiguration(CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint,
                                     CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint,
                                     CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler) throws NoSuchAlgorithmException {
            this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
            this.customBearerTokenAuthenticationEntryPoint = customBearerTokenAuthenticationEntryPoint;
            this.customBearerTokenAccessDeniedHandler = customBearerTokenAccessDeniedHandler;

            //Generate a public/private key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);//The generated key will have a size of 2048 bits
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //we will use builder pattern to customize the security for our application
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.GET, baseUrl + "/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, baseUrl + "/users/**").hasAuthority("ROLE_Admin")//only admin can get all users info
                        //.requestMatchers(HttpMethod.POST, baseUrl + "/users").permitAll()
                        .requestMatchers(HttpMethod.POST, baseUrl + "/users").hasAuthority("ROLE_Admin")
                        .requestMatchers(HttpMethod.PUT, baseUrl + "/users/**").hasAuthority("ROLE_Admin")
                        .requestMatchers(HttpMethod.DELETE, baseUrl + "/users/**").hasAuthority("ROLE_Admin")
                        .anyRequest().authenticated()//always a good idea to put this as last for example author api end points

                )
                .csrf(csrf -> csrf.disable())//disable to allow post or put request to the server
                .cors(Customizer.withDefaults())
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(this.customBasicAuthenticationEntryPoint))//enable basic authentication
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt().and()
                        .authenticationEntryPoint(this.customBearerTokenAuthenticationEntryPoint)
                        .accessDeniedHandler(this.customBearerTokenAccessDeniedHandler))
                //.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(Customizer.withDefaults()))
                //TURN OFF SESSION SINCE WE ARE NOW USING JWT
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    //password encoder using Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);//12 is a good number, too big number will make encryption slow

    }

    //jwt encoder using Bcrypt
    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
        JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSet);

    }

    //jwt decoder to verify token
    @Bean
    public JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); //by default, the prefix is SCOPE, so set it to empty

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;


    }

}

/*
* admin user (has authority to view all authors)
* userName:tayo-->Admin user
* password:654321
* userName:timi --->user
* password: 123456
* */


/*
* we will define all custom security rules and configurations here
* @Bean will make sure spring injects it to whoever needs it.
* this to login via the window terminal
* curl -X POST http://localhost:8081/api/v1/login -u john:123456 -v
* */