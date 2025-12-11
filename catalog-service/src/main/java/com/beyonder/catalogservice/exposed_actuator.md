# Resilience4j Actuator Metrics Guide

Panduan lengkap untuk mengekspos dan memonitor metrik Resilience4j menggunakan Spring Boot Actuator.

## 📋 Table of Contents

- [Dependencies](#dependencies)
- [Configuration](#configuration)
- [Controller Implementation](#controller-implementation)
- [Testing Endpoints](#testing-endpoints)
- [Metrics Reference](#metrics-reference)
- [Test Scripts](#test-scripts)

---

## 🔧 Dependencies

### pom.xml

```xml
<!-- Spring Boot Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Resilience4j -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- Micrometer for metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Spring Boot AOP (required for annotations) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

## ⚙️ Configuration

### application.properties

```properties
# Resilience4j Retry Configuration
resilience4j.retry.instances.catalogRetry.max-attempts=3
resilience4j.retry.instances.catalogRetry.wait-duration=1s

# Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.catalogBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.catalogBreaker.sliding-window-size=10
resilience4j.circuitbreaker.instances.catalogBreaker.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.catalogBreaker.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.instances.catalogBreaker.register-health-indicator=true

# Actuator Configuration - Expose All Endpoints
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.metrics.enabled=true
management.endpoint.prometheus.enabled=true

# Health Indicators
management.health.circuitbreakers.enabled=true
management.health.ratelimiters.enabled=true

# Metrics Export
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
```

### Configuration Explanation

| Property | Value | Description |
|----------|-------|-------------|
| `failure-rate-threshold` | 50 | Circuit opens if 50% of calls fail |
| `sliding-window-size` | 10 | Calculate failure rate from last 10 calls |
| `wait-duration-in-open-state` | 10s | Wait 10 seconds before trying again |
| `permitted-number-of-calls-in-half-open-state` | 3 | Test with 3 calls in HALF_OPEN state |
| `register-health-indicator` | true | Enable health endpoint monitoring |

---

## 📝 Controller Implementation

### CatalogController.java

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

---

## 🧪 Testing Endpoints

### 1. Check All Available Endpoints

```bash
curl http://localhost:8080/actuator | jq
```

**Response:**
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

---

### 2. Circuit Breaker Health

```bash
curl http://localhost:8080/actuator/health | jq
```

**Response:**
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
            "slowCallRateThreshold": "100.0%",
            "bufferedCalls": 10,
            "failedCalls": 4,
            "slowCalls": 0,
            "slowFailedCalls": 0,
            "notPermittedCalls": 0,
            "state": "CLOSED"
          }
        }
      }
    }
  }
}
```

**Circuit Breaker States:**
- `CLOSED` - Normal operation, requests pass through
- `OPEN` - Too many failures, requests blocked (go to fallback)
- `HALF_OPEN` - Testing if service recovered

---

### 3. List All Metrics

```bash
curl http://localhost:8080/actuator/metrics | jq
```

**Response (partial):**
```json
{
  "names": [
    "resilience4j.circuitbreaker.calls",
    "resilience4j.circuitbreaker.state",
    "resilience4j.circuitbreaker.failure.rate",
    "resilience4j.circuitbreaker.buffered.calls",
    "resilience4j.retry.calls"
  ]
}
```

---

### 4. Circuit Breaker State

```bash
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state | jq
```

**Response:**
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
- `0.0` = CLOSED (normal)
- `1.0` = OPEN (circuit tripped)
- `2.0` = HALF_OPEN (testing)

---

### 5. Circuit Breaker Calls

```bash
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls | jq
```

**Response:**
```json
{
  "name": "resilience4j.circuitbreaker.calls",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 45.0
    }
  ],
  "availableTags": [
    {
      "tag": "kind",
      "values": ["successful", "failed", "not_permitted"]
    },
    {
      "tag": "name",
      "values": ["catalogBreaker"]
    }
  ]
}
```

---

### 6. Detailed Calls by Kind

#### Successful Calls
```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:successful" | jq
```

#### Failed Calls
```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:failed" | jq
```

#### Not Permitted (Circuit is OPEN)
```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:not_permitted" | jq
```

**Response:**
```json
{
  "name": "resilience4j.circuitbreaker.calls",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 23.0
    }
  ]
}
```

---

### 7. Failure Rate

```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate?tag=name:catalogBreaker" | jq
```

**Response:**
```json
{
  "name": "resilience4j.circuitbreaker.failure.rate",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 43.5
    }
  ]
}
```

---

### 8. Circuit Breakers Summary

```bash
curl http://localhost:8080/actuator/circuitbreakers | jq
```

**Response:**
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

---

### 9. Retry Metrics

```bash
curl "http://localhost:8080/actuator/metrics/resilience4j.retry.calls?tag=name:catalogRetry" | jq
```

**Response:**
```json
{
  "name": "resilience4j.retry.calls",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 67.0
    }
  ],
  "availableTags": [
    {
      "tag": "kind",
      "values": ["successful_without_retry", "successful_with_retry", "failed_without_retry", "failed_with_retry"]
    }
  ]
}
```

---

### 10. Prometheus Format

```bash
curl http://localhost:8080/actuator/prometheus
```

**Response:**
```
# HELP resilience4j_circuitbreaker_calls_total Total number of calls
# TYPE resilience4j_circuitbreaker_calls_total counter
resilience4j_circuitbreaker_calls_total{kind="successful",name="catalogBreaker",} 23.0
resilience4j_circuitbreaker_calls_total{kind="failed",name="catalogBreaker",} 18.0
resilience4j_circuitbreaker_calls_total{kind="not_permitted",name="catalogBreaker",} 0.0

# HELP resilience4j_circuitbreaker_state Circuit Breaker State
# TYPE resilience4j_circuitbreaker_state gauge
resilience4j_circuitbreaker_state{name="catalogBreaker",} 0.0

# HELP resilience4j_circuitbreaker_failure_rate Failure rate
# TYPE resilience4j_circuitbreaker_failure_rate gauge
resilience4j_circuitbreaker_failure_rate{name="catalogBreaker",} 43.9

# HELP resilience4j_retry_calls_total Total number of retry calls
# TYPE resilience4j_retry_calls_total counter
resilience4j_retry_calls_total{kind="successful_without_retry",name="catalogRetry",} 15.0
resilience4j_retry_calls_total{kind="successful_with_retry",name="catalogRetry",} 8.0
resilience4j_retry_calls_total{kind="failed_with_retry",name="catalogRetry",} 3.0
```

---

### 11. Custom Statistics

```bash
curl http://localhost:8080/api/catalog/stats | jq
```

**Response:**
```json
{
  "totalAttempts": 45,
  "successCount": 23,
  "failureCount": 22,
  "successRate": 51.11,
  "timestamp": "2024-12-11T10:30:45"
}
```

---

## 📊 Metrics Reference

### Key Endpoints Summary

| Endpoint | Purpose |
|----------|---------|
| `/actuator` | List all available endpoints |
| `/actuator/health` | Overall health + circuit breaker state |
| `/actuator/metrics` | List all available metrics |
| `/actuator/circuitbreakers` | Circuit breaker details |
| `/actuator/prometheus` | Prometheus format (for monitoring) |
| `/actuator/metrics/resilience4j.circuitbreaker.state` | Circuit state (0/1/2) |
| `/actuator/metrics/resilience4j.circuitbreaker.calls` | Total calls breakdown |
| `/actuator/metrics/resilience4j.circuitbreaker.failure.rate` | Current failure rate |
| `/actuator/metrics/resilience4j.retry.calls` | Retry attempts |

### Circuit Breaker Metrics

| Metric | Description | Values |
|--------|-------------|--------|
| `resilience4j.circuitbreaker.state` | Current circuit state | 0=CLOSED, 1=OPEN, 2=HALF_OPEN |
| `resilience4j.circuitbreaker.calls` | Total calls by kind | successful, failed, not_permitted |
| `resilience4j.circuitbreaker.failure.rate` | Current failure percentage | 0-100 |
| `resilience4j.circuitbreaker.buffered.calls` | Calls in sliding window | Number |
| `resilience4j.circuitbreaker.slow.calls` | Slow call count | Number |

### Retry Metrics

| Metric | Description | Values |
|--------|-------------|--------|
| `resilience4j.retry.calls` | Retry attempts by outcome | successful_without_retry, successful_with_retry, failed_without_retry, failed_with_retry |

---

## 🚀 Test Scripts

### Complete Test Script

```bash
#!/bin/bash

echo "🧪 Testing Resilience4j Metrics"
echo "================================"

# Generate traffic
echo -e "\n📞 Generating traffic..."
for i in {1..20}; do
  curl -s http://localhost:8080/api/catalog/products/ITEM$i > /dev/null
  sleep 0.5
done

echo -e "\n✅ Traffic generated!\n"

# Check custom stats
echo "📊 Custom Statistics:"
curl -s http://localhost:8080/api/catalog/stats | jq
echo ""

# Check circuit breaker health
echo "🔍 Circuit Breaker Health:"
curl -s http://localhost:8080/actuator/health/circuitBreakers | jq
echo ""

# Check circuit breaker state
echo "🎯 Circuit Breaker State:"
curl -s http://localhost:8080/actuator/circuitbreakers | jq
echo ""

# Check failure rate metric
echo "📉 Failure Rate:"
curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate?tag=name:catalogBreaker" | jq '.measurements[0].value'
echo ""

# Check successful calls
echo "✅ Successful Calls:"
curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:successful" | jq '.measurements[0].value'
echo ""

# Check failed calls
echo "❌ Failed Calls:"
curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:failed" | jq '.measurements[0].value'
echo ""

# Check not permitted calls (when circuit is OPEN)
echo "🚫 Not Permitted Calls:"
curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:catalogBreaker&tag=kind:not_permitted" | jq '.measurements[0].value'
```

### Quick Monitoring Loop

```bash
#!/bin/bash

# Monitor circuit breaker state every 2 seconds
while true; do
  clear
  echo "🔄 Circuit Breaker Real-time Monitor"
  echo "===================================="
  echo ""
  
  STATE=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:catalogBreaker" | jq -r '.measurements[0].value')
  FAILURE_RATE=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate?tag=name:catalogBreaker" | jq -r '.measurements[0].value')
  
  echo "State: $STATE (0=CLOSED, 1=OPEN, 2=HALF_OPEN)"
  echo "Failure Rate: $FAILURE_RATE%"
  echo ""
  
  curl -s http://localhost:8080/actuator/circuitbreakers | jq '.circuitBreakers.catalogBreaker'
  
  sleep 2
done
```

### Generate Load Test

```bash
#!/bin/bash

echo "🔥 Load Testing Circuit Breaker..."

# Send 50 requests rapidly
for i in {1..50}; do
  curl -s http://localhost:8080/api/catalog/products/PROD$i > /dev/null &
done

wait

echo "✅ Load test completed!"
echo ""
echo "Check metrics with:"
echo "curl http://localhost:8080/actuator/circuitbreakers | jq"
```

---

## 📈 Integration with Monitoring Tools

### Prometheus Configuration

Add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboard

Import dashboard ID: **19469** (Spring Boot Resilience4j)

Or create custom queries:
```promql
# Circuit Breaker State
resilience4j_circuitbreaker_state{name="catalogBreaker"}

# Failure Rate
resilience4j_circuitbreaker_failure_rate{name="catalogBreaker"}

# Total Calls
sum(resilience4j_circuitbreaker_calls_total{name="catalogBreaker"})
```

---

## 🎯 Best Practices

1. **Always enable health indicators** for circuit breakers
2. **Use Prometheus endpoint** for production monitoring
3. **Set up alerts** when circuit breaker opens
4. **Monitor failure rates** to adjust thresholds
5. **Track not_permitted calls** to see how often circuit is OPEN
6. **Combine with custom business metrics** for better insights
7. **Use Grafana or similar** for visualization
8. **Export metrics** to APM tools (New Relic, Datadog, etc.)

---

## 🔗 Useful Links

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/)
- [Prometheus](https://prometheus.io/)
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)

---

## 📝 Notes

- Metrics are collected automatically when Actuator is enabled
- Circuit breaker state changes are reflected immediately
- Use `/actuator/prometheus` for production monitoring
- Health endpoint shows detailed circuit breaker status
- Custom stats endpoint provides business-level metrics

---

**Created**: December 11, 2024  
**Last Updated**: December 11, 2024  
**Version**: 1.0