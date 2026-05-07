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
        DatabaseBootstrap.initialize();
        SpringApplication.run(App.class, args);
    }
}