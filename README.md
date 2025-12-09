---

# 📘 **Spring Boot Mastery Workshop — 4 Days Intensive Training**

Pelatihan intensif 4 hari ini dirancang untuk memberikan pemahaman mendalam mengenai **Spring Boot**, **REST API**, **Clean Architecture**, **Docker & Kubernetes**, hingga **Resilience Engineering** menggunakan **Resilience4j**.
Setiap hari terdiri dari pembahasan teori dan **hands-on** yang langsung diterapkan pada mini-project sederhana.

---

## 🗓️ **Agenda Training**

---

## **Day 1 — Spring Boot Fundamentals**

### **🔹 Topik**

#### 1. Spring Boot Fundamentals & REST API (09.00 – 12.00)

* Pengenalan Spring Boot
* Spring Boot Starters & Auto Configuration
* Desain REST API & Exception Handling

#### 2. Building API Gateway Services (13.00 – 16.30)

* Perbedaan API Gateway vs aplikasi utama
* Routing & Filter concepts pada Spring Cloud Gateway

### **🛠 Hands-on**

* Membangun REST API `/products` dengan:

    * Validasi
    * Centralized error handling menggunakan `@ControllerAdvice`
* Membangun Spring Cloud Gateway dengan:

    * Basic routing
    * Path rewriting
    * Logging filter

---

## **Day 2 — API, JPA, & Clean Architecture**

### **🔹 Topik**

#### 1. REST API & Application Architecture (09.00 – 12.00)

* Entity, DTO request & response
* Struktur controller → service → repository
* Validasi input & error handling yang konsisten

#### 2. JPA Integration & Query Optimization (12.00 – 16.30)

* Repository, paging & sorting
* Custom query (`@Query`, native)
* Lazy vs Eager loading
* Transactional scope

#### 3. Data Access Best Practices

* Penanganan N+1 queries
* Clean code & pola akses data yang maintainable

### **🛠 Hands-on**

* Membangun ulang endpoint `/products`
* Menerapkan validasi dan exception handling yang baik
* Menambahkan query custom dan optimasi akses data

---

## **Day 3 — Docker & Kubernetes Deployment**

### **🔹 Topik**

#### 1. Docker & K8s Deployment Fundamentals (09.00 – 12.00)

* Dockerfile & Docker Compose
* Spring profiles & external config
* Deploy aplikasi ke Kubernetes (Minikube/OCP)

#### 2. Configuration Best Practices (13.00 – 16.30)

* Struktur YAML
* Multi-environment configuration
* Secrets management & structured logging

### **🛠 Hands-on**

* Dockerize aplikasi Spring Boot
* Deploy ke cluster Kubernetes:

    * Deployment
    * Service
    * ConfigMap
* Menerapkan profiling (dev/prod) dan secure secrets

---

## **Day 4 — Resilience & Scalability**

### **🔹 Topik**

#### 1. Resilience with Resilience4j (09.00 – 11.00)

* Retry & fallback
* Circuit breaker pattern

#### 2. Monitoring with Actuator (11.00 – 14.00)

* Mengekspos metrics (`/actuator/metrics`)
* Monitoring retry/circuit breaker status

#### 3. Autoscaling with HPA (14.00 – 16.30)

* Konsep autoscaling di Kubernetes
* Konfigurasi Horizontal Pod Autoscaler
* Observasi hasil scaling

### **🛠 Hands-on**

* Implementasi retry & fallback
* Aktivasi actuator + monitoring
* Menerapkan HPA dan simulasi load ringan

---

## 📂 **Project Structure (Sample)**

```
springboot-training/
 ├── day1/
 ├── day2/
 ├── day3/
 ├── day4/
 ├── README.md
```

---

## 📌 **Outcome Setelah Pelatihan**

Setelah mengikuti workshop ini, peserta diharapkan mampu:

✔ Membangun REST API yang bersih, terstruktur, dan maintainable
✔ Menggunakan JPA dengan benar serta menghindari N+1 dan masalah performa umum
✔ Mendockerize aplikasi Spring Boot & melakukan deployment ke Kubernetes
✔ Menggunakan Resilience4j untuk meningkatkan reliability aplikasi
✔ Melakukan monitoring & autoscaling menggunakan Actuator + Kubernetes HPA

--- 
SUBTOPIC 3.1.1:

Build image:
docker build -t my-image .
docker build -t order-service:latest .
docker run -p 8082:8081 order-service:latest
docker run -p 8082:8081 order-service:latest -d

kubectl get nodes
kubectl apply -f deployment.yml

kubectl delete deployment order-service
kubectl delete service order-service


SUBTOPIC 3.1.2:
docker compose --profile dev up --build -d