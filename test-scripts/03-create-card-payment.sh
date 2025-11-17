#!/bin/bash

# Test Script 3: Create Card Payment
# This initiates a card payment for an order

API_KEY="${API_KEY:-rpay_test_key}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
ORDER_ID="${ORDER_ID:-order_replace_me}"

echo "============================================"
echo "Test 3: Create Card Payment"
echo "============================================"
echo ""

RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: ${API_KEY}" \
  -H "X-Idempotency-Key: idem_$(uuidgen)" \
  -d '{
    "orderId": "'"${ORDER_ID}"'",
    "paymentMethod": "card",
    "paymentDetails": {
      "cardNumber": "4111111111111111",
      "cardholderName": "Test User",
      "expiryMonth": "12",
      "expiryYear": "2025",
      "cvv": "123",
      "cardType": "CREDIT"
    }
  }')

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

STATUS=$(echo "$RESPONSE" | jq -r '.status' 2>/dev/null)

if [ "$STATUS" == "PENDING" ] || [ "$STATUS" == "SUCCESS" ]; then
    echo "✓ Card payment initiated successfully!"
    echo "Status: $STATUS"
else
    echo "✗ Card payment initiation failed"
fi
