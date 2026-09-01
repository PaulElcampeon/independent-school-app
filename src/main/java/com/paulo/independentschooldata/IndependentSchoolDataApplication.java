package com.paulo.independentschooldata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IndependentSchoolDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndependentSchoolDataApplication.class, args);
    }
}
