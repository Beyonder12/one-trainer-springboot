package com.beyonder.catalogservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/catalog-expose-metric")
public class CatalogExposeMetricController {

    private AtomicInteger attemptCount = new AtomicInteger(0);
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failureCount = new AtomicInteger(0);
    private Random random = new Random();

    // API dengan Circuit Breaker + Retry
    @GetMapping("/products/{id}")
    @CircuitBreaker(name = "catalogBreaker", fallbackMethod = "getProductFallback")
    @Retry(name = "catalogRetry")
    public Map<String, Object> getProduct(@PathVariable String id) {
        int attempt = attemptCount.incrementAndGet();
        System.out.println("📞 Attempt #" + attempt + " at " + LocalDateTime.now());

        // Simulate 60% failure
        if (random.nextInt(10) < 6) {
            failureCount.incrementAndGet();
            System.out.println("❌ Failed - Total failures: " + failureCount.get());
            throw new RuntimeException("Service unavailable");
        }

        successCount.incrementAndGet();
        System.out.println("✅ Success - Total success: " + successCount.get());

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", "Product " + id);
        response.put("price", 150.00);
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    // Fallback
    public Map<String, Object> getProductFallback(String id, Throwable throwable) {
        System.out.println("🔄 FALLBACK triggered");

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", "Cached Product " + id);
        response.put("price", 0.0);
        response.put("status", "FALLBACK");
        response.put("reason", throwable.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    // Get custom stats
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", attemptCount.get());
        stats.put("successCount", successCount.get());
        stats.put("failureCount", failureCount.get());
        stats.put("successRate", calculateSuccessRate());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }

    // Reset counters
    @PostMapping("/reset-stats")
    public Map<String, String> resetStats() {
        attemptCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        return Map.of(
                "message", "Statistics reset successfully",
                "timestamp", LocalDateTime.now().toString()
        );
    }

    private double calculateSuccessRate() {
        int total = attemptCount.get();
        if (total == 0) return 0.0;
        return (successCount.get() * 100.0) / total;
    }
}