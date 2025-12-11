# Resilience4j Implementation Guide - Catalog Service

Complete guide untuk implementasi Retry, Circuit Breaker, dan Monitoring dengan Resilience4j di Spring Boot.

---

## 📋 Table of Contents

1. [Dependencies](#dependencies)
2. [Application Properties](#application-properties)
3. [Retry Implementation](#retry-implementation)
4. [Circuit Breaker Implementation](#circuit-breaker-implementation)
5. [Actuator Metrics](#actuator-metrics)
6. [Testing Guide](#testing-guide)
7. [Metrics Endpoints](#metrics-endpoints)

---

## 1. Dependencies

### pom.xml

```xml
<!-- Spring Boot AOP (Required for Resilience4j) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Resilience4j Spring Boot 3 -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- Spring Boot Actuator (for metrics) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus (for metrics export) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## 2. Application Properties

### application.properties

```properties
# ============================================
# RESILIENCE4J RETRY CONFIGURATION
# ============================================
resilience4j.retry.instances.catalogRetry.max-attempts=3
resilience4j.retry.instances.catalogRetry.wait-duration=1s

# ============================================
# CIRCUIT BREAKER CONFIGURATION
# ============================================
# Buka circuit jika failure rate >= 50%
resilience4j.circuitbreaker.instances.catalogBreaker.failure-rate-threshold=50

# Hitung dari 10 request terakhir
resilience4j.circuitbreaker.instances.catalogBreaker.sliding-window-size=10

# Tunggu 10 detik sebelum coba lagi (HALF_OPEN)
resilience4j.circuitbreaker.instances.catalogBreaker.wait-duration-in-open-state=10s

# Test dengan 3 request saat HALF_OPEN
resilience4j.circuitbreaker.instances.catalogBreaker.permitted-number-of-calls-in-half-open-state=3

# Register health indicator
resilience4j.circuitbreaker.instances.catalogBreaker.register-health-indicator=true

# ============================================
# ACTUATOR CONFIGURATION
# ============================================
# Expose all actuator endpoints
management.endpoints.web.exposure.include=*

# Show health details
management.endpoint.health.show-details=always

# Enable metrics endpoints
management.endpoint.metrics.enabled=true
management.endpoint.prometheus.enabled=true

# Enable health indicators
management.health.circuitbreakers.enabled=true
management.health.ratelimiters.enabled=true

# Prometheus metrics export
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
```

---

## 3. Retry Implementation

### Simple Retry API

```java
package com.example.catalogservice.controller;

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
        
        // Simulate random failure (70% fail rate)
        if (random.nextInt(10) < 7) {
            throw new RuntimeException("Service temporarily unavailable");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("name", "Product " + id);
        response.put("price", 99.99);
        response.put("attempt", attemptCount);
        response.put("status", "SUCCESS");
        
        attemptCount = 0;
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
        
        attemptCount = 0;
        return response;
    }
}
```

### Testing Retry

```bash
# Test retry behavior
curl http://localhost:8080/api/catalog/product/123

# Expected Console Output:
# Attempt #1 at 2024-12-11T10:30:00
# Attempt #2 at 2024-12-11T10:30:01
# Attempt #3 at 2024-12-11T10:30:02
# Fallback triggered after 3 attempts
```

---

## 4. Circuit Breaker Implementation

### Circuit Breaker API

```java
package com.example.catalogservice.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

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

    // Fallback method
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

    // Get custom statistics
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
```

### Circuit Breaker States

#### 🟢 CLOSED (Normal State)
- Semua request masuk ke service
- Monitor failure rate
- Jika failure rate ≥ 50% dari 10 calls → Buka circuit

**Console Output:**
```
📞 Attempt #1 at 2024-12-11T10:00:00
✅ Success!
📞 Attempt #2 at 2024-12-11T10:00:01
❌ Failed!
```

#### 🔴 OPEN (Circuit Terbuka)
- **TIDAK ada request ke service**
- Semua request langsung ke fallback
- Tunggu 10 detik (wait-duration-in-open-state)

**Console Output:**
```
🔄 FALLBACK triggered - Circuit may be OPEN
🔄 FALLBACK triggered - Circuit may be OPEN
```

**Response:**
```json
{
  "id": "ITEM001",
  "status": "FALLBACK",
  "reason": "CircuitBreaker 'catalogBreaker' is OPEN"
}
```

#### 🟡 HALF_OPEN (Testing State)
- Setelah 10 detik wait time
- Circuit mencoba 3 request (permitted-number-of-calls-in-half-open-state)
- Jika berhasil → CLOSED
- Jika gagal → OPEN lagi

---

## 5. Actuator Metrics

### Available Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator` | List all available endpoints |
| `/actuator/health` | Application health + circuit breaker state |
| `/actuator/metrics` | List all available metrics |
| `/actuator/circuitbreakers` | Circuit breaker details |
| `/actuator/prometheus` | Prometheus format metrics |

### Resilience4j Specific Metrics

| Metric | Description |
|--------|-------------|
| `resilience4j.circuitbreaker.state` | Circuit state (0=CLOSED, 1=OPEN, 2=HALF_OPEN) |
| `resilience4j.circuitbreaker.calls` | Total calls (successful/failed/not_permitted) |
| `resilience4j.circuitbreaker.failure.rate` | Current failure rate percentage |
| `resilience4j.circuitbreaker.buffered.calls` | Number of buffered calls |
| `resilience4j.retry.calls` | Retry attempts (successful/failed) |

---

## 6. Testing Guide

### Test 1: Check All Actuator Endpoints

```bash
curl http://localhost:8080/actuator | jq
```

**Expected Response:**
```json
{
  "_links": {
    "self": { "href": "http://localhost:8080/actuator" },
    "health": { "href": "http://localhost:8080/actuator/health" },
    "metrics": { "href": "http://localhost:8080/actuator/metrics" },
    "prometheus": { "href": "http://localhost:8080/actuator/prometheus" },
    "circuitbreakers": { "href": "http://localhost:8080/actuator/circuitbreakers" }
  }
}
```

### Test 2: Circuit Breaker Health Status

```bash
curl http://localhost:8080/actuator/health | jq
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "catalogBreaker": {
          "status": "UP",
          "details": {
            "failureRate": "45.0%",
            "slowCallRate": "0.0%",
            "failureRateThreshold": "50.0%",
            "bufferedCalls": 10,
            "failedCalls": 4,
            "slowCalls": 0,
            "notPermittedCalls": 0,
            "state": "CLOSED"
          }
        }
      }
    }
  }
}
```

### Test 3: List All Available Metrics

```bash
curl http://localhost:8080/actuator/metrics | jq
```

**Expected Response (partial):**
```json
{
  "names": [
    "resilience4j.circuitbreaker.calls",
    "resilience4j.circuitbreaker.state",
    "resilience4j.circuitbreaker.failure.rate",
    "resilience4j.circuitbreaker.buffered.calls",
    "resilience4j.retry.calls",
    "http.server.requests",
    "jvm.memory.used"
  ]
}
```

### Test 4: Circuit Breaker State

```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:catalogBreaker" | jq
```

**Expected Response:**
```json
{
  "name": "resilience4j.circuitbreaker.state",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 0.0
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": ["catalogBreaker"]
    }
  ]
}
```

**State Values:**
- `0.0` = CLOSED
- `1.0` = OPEN
- `2.0` = HALF_OPEN

### Test 5: Circuit Breaker Calls by Type

```bash
# Successful calls
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:successful" | jq

# Failed calls
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:failed" | jq

# Not permitted calls (when circuit is OPEN)
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:not_permitted" | jq
```

### Test 6: Failure Rate Metric

```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate?tag=name:catalogBreaker" | jq
```

**Expected Response:**
```json
{
  "name": "resilience4j.circuitbreaker.failure.rate",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 45.5
    }
  ]
}
```

### Test 7: Circuit Breaker Events

```bash
curl http://localhost:8080/actuator/circuitbreakers | jq
```

**Expected Response:**
```json
{
  "circuitBreakers": {
    "catalogBreaker": {
      "state": "CLOSED",
      "failureRate": "45.5%",
      "bufferedCalls": 11,
      "failedCalls": 5,
      "successfulCalls": 6,
      "notPermittedCalls": 0
    }
  }
}
```

### Test 8: Prometheus Format

```bash
curl http://localhost:8080/actuator/prometheus
```

**Expected Response:**
```
# HELP resilience4j_circuitbreaker_calls_total Total number of calls
# TYPE resilience4j_circuitbreaker_calls_total counter
resilience4j_circuitbreaker_calls_total{kind="successful",name="catalogBreaker",} 23.0
resilience4j_circuitbreaker_calls_total{kind="failed",name="catalogBreaker",} 18.0

# HELP resilience4j_circuitbreaker_state Circuit Breaker State
# TYPE resilience4j_circuitbreaker_state gauge
resilience4j_circuitbreaker_state{name="catalogBreaker",} 0.0

# HELP resilience4j_circuitbreaker_failure_rate Failure rate
# TYPE resilience4j_circuitbreaker_failure_rate gauge
resilience4j_circuitbreaker_failure_rate{name="catalogBreaker",} 43.9
```

### Test 9: Custom Statistics Endpoint

```bash
curl http://localhost:8080/api/catalog/stats | jq
```

**Expected Response:**
```json
{
  "totalAttempts": 47,
  "successCount": 19,
  "failureCount": 28,
  "successRate": 40.43,
  "timestamp": "2024-12-11T10:30:00"
}
```

---

## 7. Complete Test Script

### Automated Testing Script

```bash
#!/bin/bash

echo "🧪 Testing Resilience4j Circuit Breaker & Metrics"
echo "=================================================="

# Step 1: Generate traffic to trigger circuit breaker
echo -e "\n📞 Step 1: Generating traffic (20 requests)..."
for i in {1..20}; do
  echo -n "."
  curl -s http://localhost:8080/api/catalog/products/ITEM$i > /dev/null
  sleep 0.5
done
echo -e "\n✅ Traffic generated!\n"

# Step 2: Check custom statistics
echo "📊 Step 2: Custom Statistics"
echo "----------------------------"
curl -s http://localhost:8080/api/catalog/stats | jq
echo ""

# Step 3: Check circuit breaker health
echo "🔍 Step 3: Circuit Breaker Health"
echo "---------------------------------"
curl -s http://localhost:8080/actuator/health/circuitBreakers | jq
echo ""

# Step 4: Check circuit breaker state
echo "🎯 Step 4: Circuit Breaker State"
echo "--------------------------------"
curl -s http://localhost:8080/actuator/circuitbreakers | jq
echo ""

# Step 5: Check circuit state metric (0=CLOSED, 1=OPEN, 2=HALF_OPEN)
echo "🚦 Step 5: Circuit State Metric"
echo "-------------------------------"
STATE=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:catalogBreaker" | jq '.measurements[0].value')
echo "State: $STATE (0=CLOSED, 1=OPEN, 2=HALF_OPEN)"
echo ""

# Step 6: Check failure rate
echo "📉 Step 6: Failure Rate"
echo "----------------------"
FAILURE_RATE=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate?tag=name:catalogBreaker" | jq '.measurements[0].value')
echo "Failure Rate: $FAILURE_RATE%"
echo ""

# Step 7: Check successful calls
echo "✅ Step 7: Successful Calls"
echo "--------------------------"
SUCCESS=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:successful" | jq '.measurements[0].value')
echo "Successful Calls: $SUCCESS"
echo ""

# Step 8: Check failed calls
echo "❌ Step 8: Failed Calls"
echo "----------------------"
FAILED=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:failed" | jq '.measurements[0].value')
echo "Failed Calls: $FAILED"
echo ""

# Step 9: Check not permitted calls (when circuit is OPEN)
echo "🚫 Step 9: Not Permitted Calls"
echo "------------------------------"
NOT_PERMITTED=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:not_permitted" | jq '.measurements[0].value')
echo "Not Permitted Calls: $NOT_PERMITTED"
echo ""

# Summary
echo "📋 Summary"
echo "=========="
echo "Total Successful: $SUCCESS"
echo "Total Failed: $FAILED"
echo "Not Permitted: $NOT_PERMITTED"
echo "Failure Rate: $FAILURE_RATE%"
echo "Circuit State: $STATE"
echo ""

# Reset statistics
echo "🔄 Resetting statistics..."
curl -s -X POST http://localhost:8080/api/catalog/reset-stats | jq
echo ""

echo "✨ Test completed!"
```

### Save and Run Script

```bash
# Save script
chmod +x test-resilience4j.sh

# Run script
./test-resilience4j.sh
```

---

## 8. Circuit Breaker Behavior Timeline

```
Timeline: What happens during testing

Request 1-5:   ✅❌✅❌❌  (Circuit: CLOSED, monitoring failures)
Request 6-10:  ❌❌✅❌❌  (>50% fail → Circuit: OPENS!)
Request 11-20: 🔄🔄🔄🔄  (Circuit: OPEN, all to fallback, no service calls)

[Wait 10 seconds - wait-duration-in-open-state]

Request 21-23: Testing... (Circuit: HALF_OPEN)
  - If 3 calls succeed → Circuit: CLOSED
  - If any fails → Circuit: OPEN again

Back to normal or repeat cycle
```

---

## 9. Key Differences: Retry vs Circuit Breaker

| Feature | Retry | Circuit Breaker |
|---------|-------|-----------------|
| **Purpose** | Try again if single request fails | Stop calling if many requests fail |
| **Scope** | Per request | Across multiple requests |
| **When activated** | Every failed request | After threshold reached |
| **Protection** | None | Protects downstream service |
| **Fallback timing** | After all retries exhausted | Immediately when OPEN |
| **Best for** | Transient failures | Sustained outages |

---

## 10. Monitoring with Grafana/Prometheus

### Prometheus Configuration (prometheus.yml)

```yaml
scrape_configs:
  - job_name: 'catalog-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Key Metrics to Monitor

1. **Circuit Breaker State**
    - `resilience4j_circuitbreaker_state{name="catalogBreaker"}`

2. **Failure Rate**
    - `resilience4j_circuitbreaker_failure_rate{name="catalogBreaker"}`

3. **Call Counts**
    - `resilience4j_circuitbreaker_calls_total{kind="successful"}`
    - `resilience4j_circuitbreaker_calls_total{kind="failed"}`
    - `resilience4j_circuitbreaker_calls_total{kind="not_permitted"}`

4. **Retry Metrics**
    - `resilience4j_retry_calls_total{kind="successful_with_retry"}`
    - `resilience4j_retry_calls_total{kind="failed_with_retry"}`

---

## 11. Troubleshooting

### Circuit Breaker Not Opening

**Problem:** Circuit stays CLOSED despite many failures

**Solution:**
```properties
# Lower the threshold
resilience4j.circuitbreaker.instances.catalogBreaker.failure-rate-threshold=30

# Reduce sliding window
resilience4j.circuitbreaker.instances.catalogBreaker.sliding-window-size=5
```

### Metrics Not Showing

**Problem:** `/actuator/metrics` returns empty

**Solution:**
```properties
# Enable all actuator endpoints
management.endpoints.web.exposure.include=*
management.endpoint.metrics.enabled=true
```

### Circuit Breaker Not Registered

**Problem:** `catalogBreaker` not found in health check

**Solution:**
```properties
# Enable health indicator
resilience4j.circuitbreaker.instances.catalogBreaker.register-health-indicator=true
management.health.circuitbreakers.enabled=true
```

---

## 12. Best Practices

1. **Configuration Values**
    - Start with conservative thresholds (50%)
    - Adjust based on actual service behavior
    - Monitor and tune gradually

2. **Fallback Methods**
    - Always provide meaningful fallback responses
    - Cache previous successful responses
    - Return degraded but usable data

3. **Monitoring**
    - Set up alerts for circuit state changes
    - Monitor failure rates continuously
    - Track not_permitted calls

4. **Testing**
    - Test in staging environment first
    - Simulate various failure scenarios
    - Load test with realistic traffic patterns

5. **Documentation**
    - Document expected behavior
    - Keep runbooks for circuit open events
    - Train team on interpreting metrics

---

## 13. Additional Resources

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)
- [Prometheus](https://prometheus.io/docs/)

---

**Created:** 2024-12-11  
**Version:** 1.0  
**Service:** catalog-service  
**Author:** ThreeInnovationTech