package com.tysiac.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Pozwól na dostęp do wszystkich endpointów
                .allowedOrigins("http://localhost:3000", "http://localhost:5173") // Pozwól Reactowi (na porcie 3000 lub 5173) łączyć się
                .allowedMethods("GET", "POST", "PUT", "DELETE"); // Pozwól na te metody
    }
}