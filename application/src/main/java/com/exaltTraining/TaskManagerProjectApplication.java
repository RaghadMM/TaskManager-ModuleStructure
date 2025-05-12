package com.exaltTraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TaskManagerProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerProjectApplication.class, args);
	}

}
