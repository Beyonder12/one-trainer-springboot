package com.beyonder.catalogservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/catalog-circuit-breaker")
public class CatalogCircuitBreakerController {

    private int attemptCount = 0;
    private Random random = new Random();

    // API dengan Circuit Breaker
    @GetMapping("/inventory/{id}")
    @CircuitBreaker(name = "catalogBreaker", fallbackMethod = "getInventoryFallback")
    public Map<String, Object> getInventory(@PathVariable String id) {
        attemptCount++;
        System.out.println("📞 Call #" + attemptCount + " at " + LocalDateTime.now());

        // Simulate 60% failure rate
        if (random.nextInt(10) < 6) {
            System.out.println("❌ Failed!");
            throw new RuntimeException("Inventory service down");
        }

        System.out.println("✅ Success!");
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("stock", 100);
        response.put("status", "AVAILABLE");
        response.put("callNumber", attemptCount);
        return response;
    }

    // Fallback method
    public Map<String, Object> getInventoryFallback(String id, Throwable throwable) {
        System.out.println("🔄 FALLBACK triggered - Circuit may be OPEN");

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("stock", 0);
        response.put("status", "CIRCUIT_BREAKER_FALLBACK");
        response.put("message", "Service temporarily unavailable, using cached data");
        response.put("error", throwable.getClass().getSimpleName());
        return response;
    }

    // Check Circuit Breaker status
    @GetMapping("/circuit-status")
    public Map<String, String> getCircuitStatus() {
        return Map.of(
                "message", "Check console for circuit state",
                "time", LocalDateTime.now().toString(),
                "totalCalls", String.valueOf(attemptCount)
        );
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        attemptCount = 0;
        return Map.of("message", "Counter reset");
    }
}