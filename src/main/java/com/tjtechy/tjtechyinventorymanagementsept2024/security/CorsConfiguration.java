package com.tjtechy.tjtechyinventorymanagementsept2024.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**"); //Enable CORS for the whole application.
            }
        };
    }
}
/**
 * In summary, to enable Cors, create this class and if you are using spring,
 * also add .cors(Customizer.withDefaults()) in your SecurityConfiguration
 */