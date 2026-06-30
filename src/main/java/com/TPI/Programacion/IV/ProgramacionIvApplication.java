package com.TPI.Programacion.IV;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProgramacionIvApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgramacionIvApplication.class, args);
	}

}
