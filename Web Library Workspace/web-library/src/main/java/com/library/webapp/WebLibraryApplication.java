package com.library.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class WebLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebLibraryApplication.class, args);
	}
}
