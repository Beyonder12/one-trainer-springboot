# 🧪 Panduan Testing Circuit Breaker - Resilience4j

## 📋 Daftar Isi
1. [Konfigurasi](#konfigurasi)
2. [Skenario Testing](#skenario-testing)
3. [Circuit Breaker States](#circuit-breaker-states)
4. [Expected Behavior](#expected-behavior)
5. [Troubleshooting](#troubleshooting)

---

## Konfigurasi

### application.properties
```properties
# Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.catalogBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.catalogBreaker.sliding-window-size=10
resilience4j.circuitbreaker.instances.catalogBreaker.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.catalogBreaker.permitted-number-of-calls-in-half-open-state=3
```

### Parameter Explanation
| Parameter | Value | Meaning |
|-----------|-------|---------|
| `failure-rate-threshold` | 50 | Circuit buka jika ≥50% request gagal |
| `sliding-window-size` | 10 | Hitung dari 10 request terakhir |
| `wait-duration-in-open-state` | 10s | Tunggu 10 detik sebelum test lagi |
| `permitted-number-of-calls-in-half-open-state` | 3 | Test dengan 3 request saat HALF_OPEN |

---

## Skenario Testing

### 🧪 Test 1: Basic Single Request

**Command:**
```bash
curl http://localhost:8080/api/catalog/inventory/ITEM001
```

**Expected Response (Success):**
```json
{
  "id": "ITEM001",
  "stock": 100,
  "status": "AVAILABLE",
  "callNumber": 1
}
```

**Expected Response (Failure):**
```json
{
  "id": "ITEM001",
  "stock": 0,
  "status": "CIRCUIT_BREAKER_FALLBACK",
  "message": "Service temporarily unavailable, using cached data",
  "error": "RuntimeException"
}
```

---

### 🧪 Test 2: Rapid Fire - Trigger Circuit Breaker

**Command:**
```bash
for i in {1..20}; do
  echo "Request #$i:"
  curl http://localhost:8080/api/catalog/inventory/ITEM$i
  echo -e "\n---"
  sleep 1
done
```

**Expected Console Output:**
```
📞 Call #1 at 2024-12-11T10:00:00
✅ Success!
---
📞 Call #2 at 2024-12-11T10:00:01
❌ Failed!
---
📞 Call #3 at 2024-12-11T10:00:02
❌ Failed!
---
... (more calls)
🔄 FALLBACK triggered - Circuit may be OPEN
🔄 FALLBACK triggered - Circuit may be OPEN
```

---

### 🧪 Test 3: Pretty JSON Output (with jq)

**Command:**
```bash
for i in {1..15}; do
  echo -e "\n🔹 Request #$i:"
  curl -s http://localhost:8080/api/catalog/inventory/ITEM001 | jq
  sleep 1
done
```

**Expected Output:**
```json
🔹 Request #1:
{
  "id": "ITEM001",
  "stock": 100,
  "status": "AVAILABLE",
  "callNumber": 1
}

🔹 Request #2:
{
  "id": "ITEM001",
  "stock": 0,
  "status": "CIRCUIT_BREAKER_FALLBACK",
  "message": "Service temporarily unavailable, using cached data",
  "error": "RuntimeException"
}
```

---

### 🧪 Test 4: Check Circuit Status

**Command:**
```bash
curl http://localhost:8080/api/catalog/circuit-status
```

**Expected Response:**
```json
{
  "message": "Check console for circuit state",
  "time": "2024-12-11T10:30:00",
  "totalCalls": "15"
}
```

---

### 🧪 Test 5: Complete Test Script

**File: `test-circuit-breaker.sh`**
```bash
#!/bin/bash

echo "🧪 Testing Circuit Breaker Resilience4j"
echo "========================================"
echo ""

# Reset counter
echo "🔄 Resetting counter..."
curl -X POST http://localhost:8080/api/catalog/reset
echo -e "\n"

# Phase 1: Normal requests (Circuit CLOSED)
echo "📍 PHASE 1: Normal Requests (Circuit CLOSED)"
echo "--------------------------------------------"
for i in {1..5}; do
  echo "Request #$i:"
  curl -s http://localhost:8080/api/catalog/inventory/PROD00$i | jq -c
  sleep 1
done

echo -e "\n"

# Phase 2: More requests to trigger circuit
echo "📍 PHASE 2: Triggering Circuit Breaker"
echo "--------------------------------------"
for i in {6..15}; do
  echo "Request #$i:"
  curl -s http://localhost:8080/api/catalog/inventory/PROD0$i | jq -c
  sleep 1
done

echo -e "\n"

# Phase 3: Circuit should be OPEN now
echo "📍 PHASE 3: Circuit OPEN (All requests go to fallback)"
echo "-------------------------------------------------------"
for i in {16..20}; do
  echo "Request #$i:"
  curl -s http://localhost:8080/api/catalog/inventory/PROD0$i | jq -c
  sleep 1
done

echo -e "\n"

# Wait for circuit to transition to HALF_OPEN
echo "⏳ Waiting 10 seconds for circuit to enter HALF_OPEN state..."
sleep 10

echo -e "\n"

# Phase 4: Test HALF_OPEN state
echo "📍 PHASE 4: Circuit HALF_OPEN (Testing recovery)"
echo "------------------------------------------------"
for i in {21..23}; do
  echo "Request #$i:"
  curl -s http://localhost:8080/api/catalog/inventory/PROD0$i | jq -c
  sleep 1
done

echo -e "\n"
echo "✅ Testing completed!"
echo "Check console logs for circuit state transitions."
```

**Make executable and run:**
```bash
chmod +x test-circuit-breaker.sh
./test-circuit-breaker.sh
```

---

## Circuit Breaker States

### 🟢 CLOSED (Normal State)

**Behavior:**
- Semua request masuk ke service
- Menghitung failure rate dari 10 request terakhir
- Jika failure ≥50% → Transisi ke OPEN

**Console Output:**
```
📞 Call #1 at 2024-12-11T10:00:00
✅ Success!
📞 Call #2 at 2024-12-11T10:00:01
❌ Failed!
```

**Response:** Normal API response (success atau error)

---

### 🔴 OPEN (Circuit Terbuka)

**Behavior:**
- **TIDAK ada request ke service**
- Semua request langsung ke fallback
- State ini bertahan 10 detik
- Setelah 10 detik → Transisi ke HALF_OPEN

**Console Output:**
```
🔄 FALLBACK triggered - Circuit may be OPEN
🔄 FALLBACK triggered - Circuit may be OPEN
🔄 FALLBACK triggered - Circuit may be OPEN
```

**Response:**
```json
{
  "id": "ITEM001",
  "stock": 0,
  "status": "CIRCUIT_BREAKER_FALLBACK",
  "message": "Service temporarily unavailable, using cached data",
  "error": "RuntimeException"
}
```

---

### 🟡 HALF_OPEN (Testing Recovery)

**Behavior:**
- Circuit mengizinkan 3 request untuk test
- **Jika 3 request berhasil** → CLOSED
- **Jika ada yang gagal** → OPEN lagi (tunggu 10 detik)

**Console Output:**
```
📞 Call #21 at 2024-12-11T10:00:10
✅ Success!
📞 Call #22 at 2024-12-11T10:00:11
✅ Success!
📞 Call #23 at 2024-12-11T10:00:12
✅ Success!
→ Circuit back to CLOSED
```

---

## Expected Behavior Timeline

```
Time    Request #   State        Result          
======  ==========  ===========  ================
0s      1-5         CLOSED       ✅❌✅❌❌ (Mix)
5s      6-10        CLOSED       ❌❌✅❌❌ (>50% fail)
10s     -           → OPEN       -
11s     11-20       OPEN         🔄 All fallback
30s     -           → HALF_OPEN  -
31s     21-23       HALF_OPEN    Testing (3 calls)
        
If success → CLOSED
If fail    → OPEN (wait 10s again)
```

---

## Testing dengan Postman

### Setup Collection

1. **Create Request:**
    - Method: `GET`
    - URL: `http://localhost:8080/api/catalog/inventory/ITEM001`

2. **Collection Runner:**
    - Iterations: `20`
    - Delay: `1000ms`

3. **Observe:**
    - First 5-10 requests: Mix of success/failure
    - After circuit opens: All return fallback response
    - After 10 seconds: Circuit tries to recover

---

## Troubleshooting

### ❓ Circuit tidak pernah OPEN?

**Kemungkinan:**
- Failure rate < 50%
- Belum cukup 10 request untuk sliding window

**Solution:**
```bash
# Increase failure simulation di controller
if (random.nextInt(10) < 8) {  // 80% failure rate
    throw new RuntimeException("Inventory service down");
}
```

---

### ❓ Tidak lihat transisi state di console?

**Solution:**
Tambahkan logging di `application.properties`:
```properties
logging.level.io.github.resilience4j=DEBUG
```

---

### ❓ Fallback tidak terpanggil?

**Checklist:**
1. ✅ Method signature sama + Throwable parameter
2. ✅ Nama method sesuai di `fallbackMethod` annotation
3. ✅ Class yang sama dengan method utama

---

## Quick Reference Commands

```bash
# Single request
curl http://localhost:8080/api/catalog/inventory/ITEM001

# Rapid fire testing
for i in {1..20}; do curl http://localhost:8080/api/catalog/inventory/ITEM$i; sleep 0.5; done

# Check status
curl http://localhost:8080/api/catalog/circuit-status

# Reset counter
curl -X POST http://localhost:8080/api/catalog/reset

# Pretty JSON output
curl -s http://localhost:8080/api/catalog/inventory/ITEM001 | jq
```

---

## Comparison: Retry vs Circuit Breaker

| Aspect | Retry | Circuit Breaker |
|--------|-------|-----------------|
| **Purpose** | Coba lagi jika gagal | Stop calling jika banyak gagal |
| **Scope** | Per request | Across multiple requests |
| **Protection** | None | Protects downstream service |
| **When fallback** | After all retries | Immediately when OPEN |
| **Recovery** | N/A | Automatic after wait duration |
| **Best for** | Transient failures | Sustained outages |

---

## Kesimpulan

Circuit Breaker melindungi service dari:
- ⚡ Overload
- 🔥 Cascading failures
- 💥 Resource exhaustion

**Key Points:**
1. Monitor failure rate (50% threshold)
2. Open circuit after threshold
3. Wait 10 seconds
4. Test with 3 requests (HALF_OPEN)
5. Close if successful, reopen if failed

---

**Created:** 2024-12-11  
**Version:** 1.0  
**Author:** Resilience4j Testing Guide