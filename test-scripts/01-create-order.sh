#!/bin/bash

# Test Script 1: Create Order
# This creates a new order for payment

API_KEY="${API_KEY:-rpay_test_key}"
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "============================================"
echo "Test 1: Create Order"
echo "============================================"
echo ""

# Replace with your actual merchant ID after first run
MERCHANT_ID="${MERCHANT_ID:-merch_replace_me}"

RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/orders" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: ${API_KEY}" \
  -d '{
    "merchantId": "'"${MERCHANT_ID}"'",
    "amount": 1000.00,
    "currency": "INR",
    "customerEmail": "customer@example.com",
    "customerPhone": "+919876543210",
    "receipt": "order_rcpt_'$(date +%s)'",
    "notes": "Test order from script"
  }')

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# Extract order ID
ORDER_ID=$(echo "$RESPONSE" | jq -r '.id' 2>/dev/null)

if [ "$ORDER_ID" != "null" ] && [ -n "$ORDER_ID" ]; then
    echo "✓ Order created successfully!"
    echo "Order ID: $ORDER_ID"
    echo ""
    echo "Save this for the next step:"
    echo "export ORDER_ID=$ORDER_ID"
else
    echo "✗ Failed to create order"
    exit 1
fi
