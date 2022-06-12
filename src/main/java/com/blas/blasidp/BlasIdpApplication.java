package com.blas.blasidp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@EntityScan("com.blas.blascommon")
@SpringBootApplication
public class BlasIdpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlasIdpApplication.class, args);
        System.out.println("fdf");
    }

}
