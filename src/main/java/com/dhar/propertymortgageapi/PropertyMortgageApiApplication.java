package com.dhar.propertymortgageapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Property Mortgage API application.
 */
@SpringBootApplication
@EnableCaching
public class PropertyMortgageApiApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PropertyMortgageApiApplication.class, args);
    }
}