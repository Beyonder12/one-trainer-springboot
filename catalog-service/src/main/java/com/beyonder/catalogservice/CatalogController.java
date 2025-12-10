package com.beyonder.catalogservice;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private int attemptCount = 0;
    private Random random = new Random();

    @GetMapping("/product/{id}")
    @Retry(name = "catalogRetry", fallbackMethod = "getProductFallback")
    public Map<String, Object> getProduct(@PathVariable String id) {
        attemptCount++;
        System.out.println("Attempt #" + attemptCount + " at " + LocalDateTime.now());
        
        // Simulate random failure (70% fail rate for testing)
        if (random.nextInt(10) < 7) {
            throw new RuntimeException("Service temporarily unavailable");
        }
        
        // Success response
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", "Product " + id);
        response.put("price", 99.99);
        response.put("attempt", attemptCount);
        response.put("status", "SUCCESS");
        
        attemptCount = 0; // Reset on success
        return response;
    }

    // Fallback method - MUST have same signature + Throwable
    public Map<String, Object> getProductFallback(String id, Throwable throwable) {
        System.out.println("Fallback triggered after " + attemptCount + " attempts");
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", "Cached Product " + id);
        response.put("price", 0.0);
        response.put("status", "FALLBACK");
        response.put("error", throwable.getMessage());
        response.put("totalAttempts", attemptCount);
        
        attemptCount = 0; // Reset
        return response;
    }

    // Test endpoint to reset counter
    @PostMapping("/reset")
    public Map<String, String> reset() {
        attemptCount = 0;
        return Map.of("message", "Counter reset", "time", LocalDateTime.now().toString());
    }
}