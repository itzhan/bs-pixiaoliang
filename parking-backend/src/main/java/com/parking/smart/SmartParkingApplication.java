package com.parking.smart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.parking.smart.mapper")
public class SmartParkingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartParkingApplication.class, args);
    }
}
