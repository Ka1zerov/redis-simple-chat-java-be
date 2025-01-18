package com.ka1zerov.nbd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

/**
 * Entry point for the NBD Application.
 * Configures the server port from environment variables or defaults to 8080.
 */
@SpringBootApplication
public class NbdApplication {

	public static void main(String[] args) {
		// Retrieve port from environment variables or use default.
		String port = System.getenv().getOrDefault("PORT", "8080");

		SpringApplication app = new SpringApplication(NbdApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", port));

		app.run(args);
	}
}
