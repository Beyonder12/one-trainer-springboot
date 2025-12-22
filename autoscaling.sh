#!/bin/bash

# Master Autoscaling Script - One Stop Shop
# This script provides a menu to manage autoscaling setup and tests

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

print_banner() {
    clear
    echo -e "${BLUE}"
    echo "======================================"
    echo "  Kubernetes Autoscaling Manager"
    echo "======================================"
    echo -e "${NC}"
    echo ""
}

print_menu() {
    echo "Choose an option:"
    echo ""
    echo "  1) Setup autoscaling (build image + deploy)"
    echo "  2) Run autoscaling test"
    echo "  3) Setup + Test (complete flow)"
    echo "  4) View current status"
    echo "  5) Clean up everything"
    echo "  6) Exit"
    echo ""
    read -p "Enter your choice (1-6): " choice
}

setup_autoscaling() {
    echo ""
    echo -e "${YELLOW}Starting autoscaling setup...${NC}"
    echo ""
    bash "$SCRIPT_DIR/setup-autoscaling.sh"
}

test_autoscaling() {
    echo ""
    echo -e "${YELLOW}Starting autoscaling test...${NC}"
    echo ""
    bash "$SCRIPT_DIR/test-autoscaling.sh"
}

view_status() {
    echo ""
    echo -e "${BLUE}[i]${NC} Current Status"
    echo ""

    echo "HPA Status:"
    kubectl get hpa autoscale-hpa 2>/dev/null || echo "  No HPA found"
    echo ""

    echo "Deployment Status:"
    kubectl get deployment shipping-service 2>/dev/null || echo "  No deployment found"
    echo ""

    echo "Service Status:"
    kubectl get svc shipping-service 2>/dev/null || echo "  No service found"
    echo ""

    echo "Pod Status:"
    kubectl get pods | grep shipping-service 2>/dev/null || echo "  No pods found"
    echo ""

    echo "Metrics Server Status:"
    kubectl get deployment metrics-server -n kube-system 2>/dev/null || echo "  Metrics server not deployed"
    echo ""

    echo "Service Endpoint:"
    echo "  http://localhost:30083/api/v1/shippings/hello"
    echo ""
}

cleanup_everything() {
    echo ""
    read -p "Are you sure you want to delete all autoscaling resources? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        echo "Cleanup cancelled"
        return
    fi

    echo ""
    echo -e "${YELLOW}Cleaning up...${NC}"

    kubectl delete hpa autoscale-hpa --ignore-not-found=true > /dev/null 2>&1
    echo -e "${GREEN}[✓]${NC} HPA deleted"

    kubectl delete deployment shipping-service --ignore-not-found=true > /dev/null 2>&1
    echo -e "${GREEN}[✓]${NC} Deployment deleted"

    kubectl delete service shipping-service --ignore-not-found=true > /dev/null 2>&1
    echo -e "${GREEN}[✓]${NC} Service deleted"

    kubectl delete pod load-generator --ignore-not-found=true > /dev/null 2>&1
    echo -e "${GREEN}[✓]${NC} Load generator deleted"

    echo ""
    echo -e "${GREEN}All autoscaling resources have been removed${NC}"
    echo ""
}

# Main loop
while true; do
    print_banner
    print_menu

    case $choice in
        1)
            setup_autoscaling
            ;;
        2)
            test_autoscaling
            ;;
        3)
            setup_autoscaling
            echo ""
            echo -e "${YELLOW}Setup complete! Starting test in 10 seconds...${NC}"
            sleep 10
            test_autoscaling
            ;;
        4)
            view_status
            read -p "Press Enter to continue..."
            ;;
        5)
            cleanup_everything
            read -p "Press Enter to continue..."
            ;;
        6)
            echo "Exiting..."
            exit 0
            ;;
        *)
            echo -e "${YELLOW}Invalid choice. Please try again.${NC}"
            sleep 2
            ;;
    esac
done
