#!/bin/bash

# Run all tests in sequence

echo "=========================================="
echo "RPay Complete Payment Flow Test"
echo "=========================================="
echo ""

# Check prerequisites
if ! command -v curl &> /dev/null; then
    echo "Error: curl is not installed"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "Warning: jq is not installed. Install it for better output formatting"
    echo "  Ubuntu/Debian: sudo apt-get install jq"
    echo "  macOS: brew install jq"
    echo ""
fi

# Check API key
if [ -z "$API_KEY" ]; then
    echo "Error: API_KEY environment variable not set"
    echo "Get your API key from application startup logs"
    echo "Then run: export API_KEY=your_key_here"
    exit 1
fi

if [ -z "$MERCHANT_ID" ]; then
    echo "Error: MERCHANT_ID environment variable not set"
    echo "Get your merchant ID from application startup logs"
    echo "Then run: export MERCHANT_ID=your_merchant_id"
    exit 1
fi

echo "Configuration:"
echo "  API Key: ${API_KEY:0:20}..."
echo "  Merchant ID: $MERCHANT_ID"
echo "  Base URL: ${BASE_URL:-http://localhost:8080}"
echo ""

# Test 1: Create Order
echo "Step 1: Creating order..."
./01-create-order.sh
if [ $? -ne 0 ]; then
    echo "Failed at step 1"
    exit 1
fi

# Extract ORDER_ID from previous command output
# (You may need to manually set this)
read -p "Enter the ORDER_ID from above: " ORDER_ID
export ORDER_ID

echo ""
echo "Step 2: Creating UPI payment..."
./02-create-upi-payment.sh

echo ""
echo "Step 3: Creating new order for card payment..."
./01-create-order.sh

read -p "Enter the new ORDER_ID for card payment: " ORDER_ID
export ORDER_ID

echo ""
echo "Step 4: Creating Card payment..."
./03-create-card-payment.sh

echo ""
echo "=========================================="
echo "All tests completed!"
echo "=========================================="
