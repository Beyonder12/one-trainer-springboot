# Kubernetes Autoscaling - Quick Start Guide

## One-Button Setup & Test

There are **3 ways** to run autoscaling:

---

## 🎯 Option 1: Interactive Menu (Recommended)

**One button to rule them all!**

```bash
./autoscaling.sh
```

This opens an interactive menu with options to:
- ✅ Setup autoscaling
- ✅ Run tests
- ✅ View status
- ✅ Clean up

**Example:**
```
Choose an option:

  1) Setup autoscaling (build image + deploy)
  2) Run autoscaling test
  3) Setup + Test (complete flow)
  4) View current status
  5) Clean up everything
  6) Exit
```

---

## 🚀 Option 2: Direct Commands

### Setup Autoscaling
```bash
./setup-autoscaling.sh
```

**What it does:**
1. ✅ Builds Docker image
2. ✅ Compiles with Maven
3. ✅ Deploys Metrics Server
4. ✅ Deploys shipping-service
5. ✅ Configures HPA
6. ✅ Verifies everything works

**Time:** ~2-3 minutes

---

### Test Autoscaling
```bash
./test-autoscaling.sh
```

**What it does:**
1. ✅ Checks initial pod count
2. ✅ Starts load generator
3. ✅ Monitors scale-up (90 seconds)
4. ✅ Stops load generator
5. ✅ Monitors scale-down (5+ minutes)
6. ✅ Shows test results

**Time:** ~7-9 minutes

---

## 🔥 Option 3: Complete Flow

Setup + Test in one go:
```bash
./setup-autoscaling.sh && ./test-autoscaling.sh
```

This does everything automatically!

---

## 📊 What You'll See

### During Setup:
```
[✓] Kubernetes cluster is accessible
[✓] Maven build completed
[✓] Docker image built successfully
[✓] Metrics Server deployed
[✓] Shipping service is ready
[✓] HPA deployed
```

### During Test:
```
[i] Step 4: Monitoring scale-up (waiting up to 90 seconds)...

[1/9] Running Pods: 1 | HPA Replicas: 1
[2/9] Running Pods: 1 | HPA Replicas: 1
[3/9] Running Pods: 3 | HPA Replicas: 3
[✓] Scale-up detected! Pods increased to 3
```

---

## 📋 Configuration

Default HPA Settings:
- **Min Replicas:** 1
- **Max Replicas:** 3
- **CPU Threshold:** 20%
- **Service Port:** 30083

---

## 🔍 View Status Anytime

```bash
# View HPA status
kubectl get hpa

# Watch pods scale up/down
kubectl get pods -w | grep shipping-service

# Check real-time metrics
kubectl top pods

# See all resources
kubectl get all
```

---

## 🧹 Clean Up

Option 1 - Interactive:
```bash
./autoscaling.sh    # Choose option 5
```

Option 2 - Direct:
```bash
kubectl delete hpa autoscale-hpa
kubectl delete deployment shipping-service
kubectl delete service shipping-service
```

---

## 🧪 Manual Test (Step by Step)

If you want to test manually:

**1. Start load:**
```bash
kubectl run load-generator --image=busybox --restart=Never -- \
  /bin/sh -c "while true; do wget -q -O- http://shipping-service:8083/api/v1/shippings/hello; done"
```

**2. Watch scale-up (in another terminal):**
```bash
kubectl get hpa -w
```

**3. Stop load:**
```bash
kubectl delete pod load-generator
```

**4. Watch scale-down:**
```bash
kubectl get hpa -w
```

---

## ✅ Success Indicators

**Scale-up working:**
- CPU usage > 20%
- Pod count increases from 1 → 3
- HPA replicas match pod count

**Scale-down working:**
- CPU usage < 20%
- Pod count decreases from 3 → 1
- Takes 5+ minutes due to stabilization window

---

## 🆘 Troubleshooting

### Pods won't scale up
```bash
# Check if metrics are being collected
kubectl top pods

# Check HPA status
kubectl describe hpa autoscale-hpa

# View HPA events
kubectl get events | grep HPA
```

### Service not responding
```bash
# Check service
kubectl get svc shipping-service

# Check pods
kubectl get pods | grep shipping-service

# Test endpoint
curl http://localhost:30083/api/v1/shippings/hello
```

### Metrics server not working
```bash
# Check metrics server
kubectl get deployment metrics-server -n kube-system

# Redeploy if needed
kubectl apply -f component-metrics.yaml
```

---

## 🎓 Understanding HPA

**Horizontal Pod Autoscaler (HPA)** automatically scales pods based on metrics.

```
┌─────────────────────────────┐
│  CPU Usage > 20%?           │
│         ↓                   │
│  YES → Scale UP (max 3)     │
│         ↓                   │
│  More pods handle load      │
└─────────────────────────────┘

┌─────────────────────────────┐
│  CPU Usage < 20%?           │
│         ↓                   │
│  YES → Scale DOWN (min 1)   │
│         ↓                   │
│  Fewer pods needed          │
└─────────────────────────────┘
```

---

## 📞 Need Help?

**Check HPA configuration:**
```bash
kubectl get hpa autoscale-hpa -o yaml
```

**View detailed metrics:**
```bash
kubectl get hpa autoscale-hpa -o jsonpath='{.status}' | jq
```

**Stream logs:**
```bash
kubectl logs -f deployment/shipping-service
```

---

## 🚀 Quick Recap

| Task | Command |
|------|---------|
| Setup + Test | `./autoscaling.sh` |
| Just Setup | `./setup-autoscaling.sh` |
| Just Test | `./test-autoscaling.sh` |
| View Status | `kubectl get hpa` |
| Watch Pods | `kubectl get pods -w` |
| Clean Up | `./autoscaling.sh` → Option 5 |

**That's it! One button to set it all up!** 🎉
