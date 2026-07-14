// v0.1.0: Minimal application entry point to verify build/run works.
// v0.2.5: Application entry point with factory-based repository selection.
// v0.4.0: Spring Boot application entry point for the Coin Tracker API.
package com.vincevscode.cointracker;

import com.vincevscode.cointracker.db.DatabaseBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        // Schema/seed setup runs BEFORE the Spring context starts, because repository beans
        // (wired in ApplicationConfiguration) assume the tables already exist when the app
        // context is built — there's no dependency-ordered bean for "run raw SQL first".
        DatabaseBootstrap.initialize();
        SpringApplication.run(App.class, args);
    }
}