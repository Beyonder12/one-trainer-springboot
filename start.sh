#!/bin/bash

# 🚀 ULTIMATE ONE BUTTON SCRIPT
# Setup + Watch Live Scaling 1→3→1

cd "$(dirname "$0")"

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║  🚀 KUBERNETES AUTOSCALING - COMPLETE DEMO                 ║"
echo "║     Watch it scale 1 → 3 → 1 in REAL TIME!                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "This will:"
echo "  1. Setup everything (2-3 minutes)"
echo "  2. Watch pods scale UP from 1→3 under load"
echo "  3. Watch pods scale DOWN from 3→1 when idle"
echo ""

read -p "Press Enter to start..."
echo ""

# Setup
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 SETUP PHASE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
bash setup-autoscaling.sh

echo ""
echo "✓ Setup complete! Now watch the magic happen..."
sleep 2

# Watch scaling
bash watch-scaling.sh
