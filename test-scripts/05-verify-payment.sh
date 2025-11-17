#!/bin/bash

# Test Script 5: Verify Payment
# This verifies payment status with gateway

API_KEY="${API_KEY:-rpay_test_key}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
PAYMENT_ID="${PAYMENT_ID:-pay_replace_me}"

echo "============================================"
echo "Test 5: Verify Payment"
echo "============================================"
echo ""

RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/payments/${PAYMENT_ID}/verify" \
  -H "X-Api-Key: ${API_KEY}")

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

STATUS=$(echo "$RESPONSE" | jq -r '.status' 2>/dev/null)
echo "Payment Status: $STATUS"
