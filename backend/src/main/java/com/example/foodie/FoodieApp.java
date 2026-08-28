package com.example.foodie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodieApp {

	public static void main(String[] args) {
		SpringApplication.run(FoodieApp.class, args);

	}

}
