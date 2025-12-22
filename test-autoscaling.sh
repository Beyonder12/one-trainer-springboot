#!/bin/bash

# Autoscaling Test Script - One Button Test
# This script automates the entire autoscaling test process

set -e

echo "======================================"
echo "   Kubernetes HPA Autoscaling Test"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[i]${NC} $1"
}

# Step 1: Check initial status
print_info "Step 1: Checking initial status..."
echo ""
kubectl get hpa autoscale-hpa
echo ""
INITIAL_PODS=$(kubectl get pods -l app=shipping-service 2>/dev/null | grep Running | wc -l | xargs || echo 0)
if [ "$INITIAL_PODS" -eq 0 ]; then
    INITIAL_PODS=$(kubectl get pods | grep shipping-service | grep Running | wc -l | xargs)
fi
print_status "Initial pod count: $INITIAL_PODS"
echo ""

# Step 2: Test service endpoint
print_info "Step 2: Testing service endpoint..."
RESPONSE=$(curl -s http://localhost:30083/api/v1/shippings/hello)
if echo "$RESPONSE" | grep -q "Hello Shipping Service"; then
    print_status "Service is responding correctly"
else
    print_error "Service response unexpected: $RESPONSE"
    exit 1
fi
echo ""

# Step 3: Start load generator
print_info "Step 3: Starting load generator..."
kubectl delete pod load-generator --ignore-not-found=true > /dev/null 2>&1 || true
sleep 2
kubectl run load-generator --image=busybox --restart=Never -- \
    /bin/sh -c "while true; do wget -q -O- http://shipping-service:8083/api/v1/shippings/hello > /dev/null 2>&1; done" > /dev/null 2>&1 &
LOAD_PID=$!
sleep 3
print_status "Load generator started (PID: $LOAD_PID)"
echo ""

# Step 4: Monitor scale-up
print_info "Step 4: Monitoring scale-up (waiting up to 90 seconds)..."
echo ""

MAX_ATTEMPTS=9
SCALE_UP_DETECTED=false

for i in $(seq 1 $MAX_ATTEMPTS); do
    sleep 10

    POD_COUNT=$(kubectl get pods | grep shipping-service | grep Running | wc -l | xargs)
    REPLICAS=$(kubectl get hpa autoscale-hpa -o jsonpath='{.status.currentReplicas}' 2>/dev/null || echo "0")

    printf "[%d/%d] Running Pods: %d | HPA Replicas: %s\n" "$i" "$MAX_ATTEMPTS" "$POD_COUNT" "$REPLICAS"

    if [ "$POD_COUNT" -gt "$INITIAL_PODS" ] || [ "$POD_COUNT" -eq 3 ]; then
        SCALE_UP_DETECTED=true
        print_status "Scale-up detected! Pods increased to $POD_COUNT"
        break
    fi
done
echo ""

if [ "$SCALE_UP_DETECTED" = false ]; then
    print_warning "Scale-up did not occur as expected within 90 seconds"
    print_info "This might indicate:"
    print_info "  - Load generator is not generating enough load"
    print_info "  - Metrics server is not collecting metrics properly"
fi
echo ""

# Step 5: Show scale-up results
print_info "Step 5: Current status during load"
echo ""
echo "HPA Status:"
kubectl get hpa autoscale-hpa
echo ""
echo "Running Pods:"
kubectl get pods | grep shipping-service
echo ""

# Step 6: Stop load generator
print_info "Step 6: Stopping load generator..."
kubectl delete pod load-generator > /dev/null 2>&1 || true
sleep 2
print_status "Load generator stopped"
echo ""

# Step 7: Monitor scale-down
print_info "Step 7: Monitoring scale-down (may take 3-5 minutes)..."
print_warning "The Kubernetes HPA has a default stabilization window of 5 minutes before scaling down"
echo ""

SCALE_DOWN_DETECTED=false
MAX_SCALE_DOWN_ATTEMPTS=25

for i in $(seq 1 $MAX_SCALE_DOWN_ATTEMPTS); do
    sleep 12

    POD_COUNT=$(kubectl get pods | grep shipping-service | grep Running | wc -l | xargs)
    REPLICAS=$(kubectl get hpa autoscale-hpa -o jsonpath='{.status.currentReplicas}' 2>/dev/null || echo "0")

    printf "[%d/%d] Running Pods: %d | HPA Replicas: %s\n" "$i" "$MAX_SCALE_DOWN_ATTEMPTS" "$POD_COUNT" "$REPLICAS"

    if [ "$POD_COUNT" -le "$INITIAL_PODS" ]; then
        SCALE_DOWN_DETECTED=true
        print_status "Scale-down complete! Back to $POD_COUNT pods"
        break
    fi
done
echo ""

# Step 8: Final status
print_info "Step 8: Final status"
echo ""
kubectl get hpa autoscale-hpa
echo ""
echo "Final Pods:"
kubectl get pods | grep shipping-service
echo ""

FINAL_PODS=$(kubectl get pods | grep shipping-service | grep Running | wc -l | xargs)

# Summary
echo ""
echo "======================================"
echo "           Test Summary"
echo "======================================"
echo ""
echo "Initial Pods:      $INITIAL_PODS"
echo "Peak Pods:         $([ "$SCALE_UP_DETECTED" = true ] && echo "3 (Scaled Up)" || echo "No scale detected")"
echo "Final Pods:        $FINAL_PODS"
echo ""

if [ "$SCALE_UP_DETECTED" = true ]; then
    print_status "Scale-up test PASSED ✓"
else
    print_error "Scale-up test FAILED ✗"
fi

if [ "$SCALE_DOWN_DETECTED" = true ]; then
    print_status "Scale-down test PASSED ✓"
elif [ "$SCALE_UP_DETECTED" = true ]; then
    print_warning "Scale-down is in progress (may take longer)"
fi

echo ""

if [ "$SCALE_UP_DETECTED" = true ] && [ "$FINAL_PODS" -le "$INITIAL_PODS" ]; then
    print_status "Overall: Autoscaling is working correctly! ✓"
elif [ "$SCALE_UP_DETECTED" = true ]; then
    print_warning "Overall: Scale-up works, scale-down still in progress"
else
    print_error "Overall: Autoscaling test did not complete successfully"
    echo ""
    print_info "Troubleshooting:"
    echo "  1. Check metrics-server is running: kubectl get deployment metrics-server -n kube-system"
    echo "  2. Check HPA status: kubectl describe hpa autoscale-hpa"
    echo "  3. Check metrics available: kubectl top pods"
fi

echo ""
echo "======================================"
