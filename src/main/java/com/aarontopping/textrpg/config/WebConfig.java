package com.aarontopping.textrpg.config; // Your package

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// I set this up so I can handle configuration locally and on deployment.
@Configuration
public class WebConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500}")
    private String[] allowedOriginsProperty;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                List<String> origins = Arrays.stream(allowedOriginsProperty)
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .collect(Collectors.toList());

                if (origins.isEmpty()) {
                    System.err.println("Warning: No CORS origins specified or property is empty!");
                } else {
                    registry.addMapping("/api/**")
                            .allowedOrigins(origins.toArray(new String[0]))
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                            .allowedHeaders("*")
                            .allowCredentials(false);
                }
            }
        };
    }
}