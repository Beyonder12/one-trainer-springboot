#!/bin/bash

# Setup shell aliases for easy autoscaling commands

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SHELL_RC=""

# Detect shell
if [ -n "$ZSH_VERSION" ]; then
    SHELL_RC="$HOME/.zshrc"
elif [ -n "$BASH_VERSION" ]; then
    SHELL_RC="$HOME/.bashrc"
else
    echo "Could not detect shell. Please manually add these aliases to your shell config:"
    echo ""
    echo "alias autoscaling='$SCRIPT_DIR/autoscaling.sh'"
    echo "alias autoscaling:setup='$SCRIPT_DIR/setup-autoscaling.sh'"
    echo "alias autoscaling:test='$SCRIPT_DIR/test-autoscaling.sh'"
    exit 1
fi

echo "Adding aliases to $SHELL_RC..."

# Backup original
cp "$SHELL_RC" "$SHELL_RC.backup"
echo "Backup created: $SHELL_RC.backup"

# Add aliases
cat >> "$SHELL_RC" << EOF

# Kubernetes Autoscaling Aliases
alias autoscaling='$SCRIPT_DIR/autoscaling.sh'
alias autoscaling:setup='$SCRIPT_DIR/setup-autoscaling.sh'
alias autoscaling:test='$SCRIPT_DIR/test-autoscaling.sh'

EOF

echo ""
echo "✅ Aliases added successfully!"
echo ""
echo "Now you can use:"
echo "  autoscaling          - Interactive menu"
echo "  autoscaling:setup    - Setup only"
echo "  autoscaling:test     - Test only"
echo ""
echo "Reload your shell: source $SHELL_RC"
echo ""
