package com.veltro.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VeltroApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeltroApplication.class, args);
    }

}
