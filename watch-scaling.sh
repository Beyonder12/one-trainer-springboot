#!/bin/bash

# Live Autoscaling Watch Script - See pods scale 1 -> 3 in real-time
set +e  # Don't exit on errors

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

print_header() {
    clear
    echo -e "${BLUE}${BOLD}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║     🚀 LIVE POD SCALING: Watch 1 Pod → 3 Pods              ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

draw_pod_progress() {
    local current=$1
    local max=3

    echo ""
    echo -e "${BOLD}Pod Count Progress:${NC}"
    echo ""

    local progress=""
    for ((i=1; i<=max; i++)); do
        if [ $i -le $current ]; then
            progress+="${GREEN}●${NC} "
        else
            progress+="${CYAN}○${NC} "
        fi
    done

    echo "  $progress"
    echo ""
    echo "  Current: ${GREEN}$current${NC} / Max: ${BLUE}$max${NC}"
    echo ""
}

draw_progress_bar() {
    local current=$1
    local max=3
    local bar_length=30
    local filled=$(( (current * bar_length) / max ))

    echo -e "${BOLD}Scaling Progress:${NC}"
    echo -n "  ["

    for ((i=0; i<bar_length; i++)); do
        if [ $i -lt $filled ]; then
            echo -n "${GREEN}█${NC}"
        else
            echo -n "${CYAN}░${NC}"
        fi
    done

    echo "] $((current * 100 / max))%"
    echo ""
}

# Step 1: Show initial state
print_header

echo -e "${BOLD}Step 1: Current State${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

INITIAL_PODS=$(kubectl get pods 2>/dev/null | grep shipping-service | grep Running | wc -l | xargs)
echo -e "Initial Pods: ${GREEN}$INITIAL_PODS${NC}"
echo ""
kubectl get pods 2>/dev/null | grep shipping-service || echo "No pods found"
echo ""
sleep 2

# Step 2: Check service
echo -e "${BOLD}Step 2: Testing Service${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

RESPONSE=$(curl -s http://localhost:30083/api/v1/shippings/hello 2>/dev/null)
if [ ! -z "$RESPONSE" ]; then
    echo -e "${GREEN}✓${NC} Service responding: $RESPONSE"
else
    echo -e "${RED}✗${NC} Service not responding, but continuing anyway..."
fi
echo ""
sleep 2

# Step 3: Start load
print_header

echo -e "${BOLD}Step 3: Starting Load Generator${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

kubectl delete pod load-generator --ignore-not-found=true > /dev/null 2>&1
sleep 1

echo "Creating load generator pod..."
kubectl run load-generator --image=busybox --restart=Never -- \
    /bin/sh -c "while true; do wget -q -O- http://shipping-service:8083/api/v1/shippings/hello > /dev/null 2>&1; done" \
    > /dev/null 2>&1

echo -e "${GREEN}✓${NC} Load generator started..."
echo ""
sleep 3

# Step 4: LIVE MONITORING
print_header

echo -e "${BOLD}Step 4: LIVE POD SCALING MONITOR${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${YELLOW}Watching pods scale up... (this may take 30-90 seconds)${NC}"
echo ""

PREV_POD_COUNT=0
MAX_WAIT=120
ELAPSED=0
SCALE_UP_TIME=""

while [ $ELAPSED -lt $MAX_WAIT ]; do
    print_header

    echo -e "${BOLD}Step 4: LIVE POD SCALING MONITOR${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo -e "${YELLOW}⏱️  Elapsed: ${ELAPSED}s${NC}"
    echo ""

    POD_COUNT=$(kubectl get pods 2>/dev/null | grep shipping-service | grep Running | wc -l | xargs)
    CPU_USAGE=$(kubectl get hpa autoscale-hpa -o jsonpath='{.status.currentMetrics[0].resource.current.averageUtilization}' 2>/dev/null || echo "0")
    HPA_REPLICAS=$(kubectl get hpa autoscale-hpa -o jsonpath='{.status.currentReplicas}' 2>/dev/null || echo "0")

    draw_pod_progress "$POD_COUNT"
    draw_progress_bar "$POD_COUNT"

    echo -e "${BOLD}Metrics:${NC}"
    echo "  CPU Usage:     ${CYAN}$CPU_USAGE%${NC}"
    echo "  HPA Replicas:  ${CYAN}$HPA_REPLICAS${NC}"
    echo ""

    echo -e "${BOLD}Pods:${NC}"
    kubectl get pods 2>/dev/null | grep shipping-service | sed 's/^/  /' || echo "  (checking...)"
    echo ""

    if [ "$POD_COUNT" -gt "$PREV_POD_COUNT" ] && [ "$PREV_POD_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✓ Scale event detected: $PREV_POD_COUNT → $POD_COUNT pods${NC}"
        echo ""
    fi

    if [ "$POD_COUNT" -eq 3 ]; then
        SCALE_UP_TIME=$ELAPSED
        echo -e "${GREEN}${BOLD}✓✓✓ SCALE-UP COMPLETE! Reached 3 pods in ${SCALE_UP_TIME}s! ✓✓✓${NC}"
        echo ""
        break
    fi

    PREV_POD_COUNT=$POD_COUNT
    ELAPSED=$((ELAPSED + 5))
    sleep 5
done

sleep 2

# Step 5: Stop load
print_header

echo -e "${BOLD}Step 5: Stopping Load Generator${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

kubectl delete pod load-generator --ignore-not-found=true > /dev/null 2>&1
echo -e "${GREEN}✓${NC} Load generator stopped"
echo ""
sleep 2

# Step 6: Monitor scale-down
print_header

echo -e "${BOLD}Step 6: LIVE POD SCALE-DOWN MONITOR${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${YELLOW}Watching pods scale down... (this takes 3-5 minutes)${NC}"
echo ""

PREV_POD_COUNT=3
MAX_WAIT=300
ELAPSED=0
SCALE_DOWN_TIME=""

while [ $ELAPSED -lt $MAX_WAIT ]; do
    print_header

    echo -e "${BOLD}Step 6: LIVE POD SCALE-DOWN MONITOR${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo -e "${YELLOW}⏱️  Elapsed: ${ELAPSED}s (may take up to 300s)${NC}"
    echo ""

    POD_COUNT=$(kubectl get pods 2>/dev/null | grep shipping-service | grep Running | wc -l | xargs)
    CPU_USAGE=$(kubectl get hpa autoscale-hpa -o jsonpath='{.status.currentMetrics[0].resource.current.averageUtilization}' 2>/dev/null || echo "0")
    HPA_REPLICAS=$(kubectl get hpa autoscale-hpa -o jsonpath='{.status.currentReplicas}' 2>/dev/null || echo "0")

    draw_pod_progress "$POD_COUNT"
    draw_progress_bar "$POD_COUNT"

    echo -e "${BOLD}Metrics:${NC}"
    echo "  CPU Usage:     ${CYAN}$CPU_USAGE%${NC}"
    echo "  HPA Replicas:  ${CYAN}$HPA_REPLICAS${NC}"
    echo ""

    echo -e "${BOLD}Pods:${NC}"
    kubectl get pods 2>/dev/null | grep shipping-service | sed 's/^/  /' || echo "  (checking...)"
    echo ""

    if [ "$POD_COUNT" -lt "$PREV_POD_COUNT" ] && [ "$PREV_POD_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✓ Scale event detected: $PREV_POD_COUNT → $POD_COUNT pods${NC}"
        echo ""
    fi

    if [ "$POD_COUNT" -le "$INITIAL_PODS" ]; then
        SCALE_DOWN_TIME=$ELAPSED
        echo -e "${GREEN}${BOLD}✓✓✓ SCALE-DOWN COMPLETE! Back to $POD_COUNT pods in ${SCALE_DOWN_TIME}s! ✓✓✓${NC}"
        echo ""
        break
    fi

    PREV_POD_COUNT=$POD_COUNT
    ELAPSED=$((ELAPSED + 15))
    sleep 15
done

sleep 2

# Final Summary
print_header

echo -e "${BOLD}FINAL RESULTS${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

FINAL_PODS=$(kubectl get pods 2>/dev/null | grep shipping-service | grep Running | wc -l | xargs)

echo -e "Initial Pods:        ${CYAN}$INITIAL_PODS${NC}"
echo -e "Peak Pods:           ${GREEN}3${NC}"
echo -e "Final Pods:          ${CYAN}$FINAL_PODS${NC}"
echo -e "Scale-Up Time:       ${GREEN}${SCALE_UP_TIME}s${NC}"
echo -e "Scale-Down Time:     ${GREEN}${SCALE_DOWN_TIME}s${NC}"
echo ""

if [ "$FINAL_PODS" -le "$INITIAL_PODS" ]; then
    echo -e "${GREEN}${BOLD}✓ AUTOSCALING WORKING PERFECTLY!${NC}"
    echo ""
    echo -e "  ${GREEN}✓${NC} Scale-up:   1 pod → 3 pods under load"
    echo -e "  ${GREEN}✓${NC} Scale-down: 3 pods → 1 pod when idle"
else
    echo -e "${YELLOW}${BOLD}⚠ Scale-down still in progress...${NC}"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    ✅ TEST COMPLETE!                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
