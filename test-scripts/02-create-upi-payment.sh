#!/bin/bash

# Test Script 2: Create UPI Payment
# This initiates a UPI payment for an order

API_KEY="${API_KEY:-rpay_test_key}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
ORDER_ID="${ORDER_ID:-order_replace_me}"

echo "============================================"
echo "Test 2: Create UPI Payment"
echo "============================================"
echo ""

RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: ${API_KEY}" \
  -H "X-Idempotency-Key: idem_$(uuidgen)" \
  -d '{
    "orderId": "'"${ORDER_ID}"'",
    "paymentMethod": "upi",
    "paymentDetails": {
      "vpa": "customer@upi",
      "flow": "collect"
    }
  }')

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# Extract payment info
PAYMENT_ID=$(echo "$RESPONSE" | jq -r '.gatewayTransactionId' 2>/dev/null)
STATUS=$(echo "$RESPONSE" | jq -r '.status' 2>/dev/null)

if [ "$STATUS" == "PENDING" ]; then
    echo "✓ Payment initiated successfully!"
    echo "Gateway Transaction ID: $PAYMENT_ID"
    echo "Status: $STATUS"
    echo ""
    echo "Save this for verification:"
    echo "export PAYMENT_ID=$PAYMENT_ID"
else
    echo "✗ Payment initiation failed"
fi
