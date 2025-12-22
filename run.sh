#!/bin/bash

# Simple One-Button Script for Autoscaling
# Just run: ./run.sh

cd "$(dirname "$0")"

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║       Kubernetes Autoscaling - One Button Setup + Test         ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Setup
echo "📦 SETUP PHASE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
bash setup-autoscaling.sh

echo ""
echo ""

# Step 2: Test
echo "🧪 TEST PHASE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
bash test-autoscaling.sh

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                     ✅ COMPLETE!                              ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
