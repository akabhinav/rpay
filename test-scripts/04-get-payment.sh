#!/bin/bash

# Test Script 4: Get Payment Details
# This retrieves payment information

API_KEY="${API_KEY:-rpay_test_key}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
PAYMENT_ID="${PAYMENT_ID:-pay_replace_me}"

echo "============================================"
echo "Test 4: Get Payment Details"
echo "============================================"
echo ""

RESPONSE=$(curl -s -X GET "${BASE_URL}/api/v1/payments/${PAYMENT_ID}" \
  -H "X-Api-Key: ${API_KEY}")

echo "Response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""
