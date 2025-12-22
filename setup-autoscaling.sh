#!/bin/bash

# Autoscaling Setup Script - Complete One-Button Setup
# This script sets up the entire autoscaling infrastructure from scratch

set -e

echo "======================================"
echo "  Kubernetes Autoscaling Setup"
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

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Step 1: Check Kubernetes connectivity
print_info "Step 1: Checking Kubernetes connectivity..."
if ! kubectl cluster-info > /dev/null 2>&1; then
    print_error "Cannot connect to Kubernetes cluster"
    exit 1
fi
print_status "Kubernetes cluster is accessible"
echo ""

# Step 2: Build Docker image
print_info "Step 2: Building Docker image for shipping-service..."
cd "$SCRIPT_DIR/shipping-service"

if ! mvn clean package -DskipTests > /dev/null 2>&1; then
    print_error "Maven build failed"
    exit 1
fi
print_status "Maven build completed"

if ! docker build -t shipping-service:latest . > /dev/null 2>&1; then
    print_error "Docker build failed"
    exit 1
fi
print_status "Docker image built successfully: shipping-service:latest"
echo ""

# Step 3: Deploy Metrics Server
print_info "Step 3: Deploying Kubernetes Metrics Server..."

# Check if metrics-server already exists
if kubectl get deployment metrics-server -n kube-system > /dev/null 2>&1; then
    print_warning "Metrics Server already exists, skipping deployment"
else
    if kubectl apply -f "$SCRIPT_DIR/component-metrics.yaml" > /dev/null 2>&1; then
        print_status "Metrics Server deployed"

        # Wait for metrics-server to be ready
        print_info "Waiting for Metrics Server to be ready..."
        kubectl wait --for=condition=available --timeout=60s deployment/metrics-server -n kube-system > /dev/null 2>&1 || true
        sleep 5
        print_status "Metrics Server is ready"
    else
        print_error "Failed to deploy Metrics Server"
        exit 1
    fi
fi
echo ""

# Step 4: Clean up any existing deployment
print_info "Step 4: Cleaning up existing deployments (if any)..."
kubectl delete deployment shipping-service --ignore-not-found=true > /dev/null 2>&1 || true
kubectl delete service shipping-service --ignore-not-found=true > /dev/null 2>&1 || true
kubectl delete hpa autoscale-hpa --ignore-not-found=true > /dev/null 2>&1 || true
kubectl delete pod load-generator --ignore-not-found=true > /dev/null 2>&1 || true
sleep 2
print_status "Cleanup complete"
echo ""

# Step 5: Deploy shipping service
print_info "Step 5: Deploying shipping-service..."
if kubectl apply -f "$SCRIPT_DIR/deployment-shipping-service.yaml" > /dev/null 2>&1; then
    print_status "Shipping service deployment created"
else
    print_error "Failed to deploy shipping service"
    exit 1
fi

# Wait for deployment to be ready
print_info "Waiting for shipping-service pods to be ready..."
kubectl wait --for=condition=available --timeout=60s deployment/shipping-service > /dev/null 2>&1 || true
sleep 3
print_status "Shipping service is ready"
echo ""

# Step 6: Deploy HPA
print_info "Step 6: Deploying Horizontal Pod Autoscaler..."
if kubectl apply -f "$SCRIPT_DIR/deployment-autoscalling-shipping-service.yaml" > /dev/null 2>&1; then
    print_status "HPA deployed"
else
    print_error "Failed to deploy HPA"
    exit 1
fi
sleep 2
echo ""

# Step 7: Verify setup
print_info "Step 7: Verifying setup..."
echo ""

echo "HPA Status:"
kubectl get hpa autoscale-hpa
echo ""

echo "Deployment Status:"
kubectl get deployment shipping-service
echo ""

echo "Service Status:"
kubectl get svc shipping-service
echo ""

echo "Pod Status:"
kubectl get pods | grep shipping-service
echo ""

# Step 8: Test service endpoint
print_info "Step 8: Testing service endpoint..."
sleep 3
RESPONSE=$(curl -s http://localhost:30083/api/v1/shippings/hello 2>/dev/null || echo "")
if echo "$RESPONSE" | grep -q "Hello Shipping Service"; then
    print_status "Service is responding correctly"
else
    print_warning "Service may not be responding yet (still warming up)"
fi
echo ""

# Summary
echo "======================================"
echo "     Setup Complete!"
echo "======================================"
echo ""
print_status "Autoscaling infrastructure is ready"
echo ""
echo "Quick Start Commands:"
echo "  • View metrics: kubectl top pods"
echo "  • Watch HPA: kubectl get hpa -w"
echo "  • Watch pods: kubectl get pods -w | grep shipping-service"
echo "  • Test service: curl http://localhost:30083/api/v1/shippings/hello"
echo ""
echo "Run Autoscaling Test:"
echo "  bash $SCRIPT_DIR/test-autoscaling.sh"
echo ""
echo "Configuration:"
echo "  • Min replicas: 1"
echo "  • Max replicas: 3"
echo "  • CPU threshold: 20%"
echo "  • Service port: 30083"
echo ""
echo "======================================"
