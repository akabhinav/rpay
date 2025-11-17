# RPay Test Scripts

Automated test scripts for testing the complete payment flow locally.

## Prerequisites

- `curl` installed
- `jq` installed (for JSON formatting)
- RPay application running on `http://localhost:8080`
- PostgreSQL and Redis running

## Setup

1. Make scripts executable:
```bash
chmod +x test-scripts/*.sh
```

2. Get your API key from application logs when it starts (see DataSeeder output)

3. Set environment variables:
```bash
export API_KEY="your_api_key_from_logs"
export MERCHANT_ID="your_merchant_id_from_logs"
```

## Running Tests

### Complete Flow Test

Run all tests in sequence:
```bash
cd test-scripts
./run-all-tests.sh
```

### Individual Tests

#### 1. Create Order
```bash
./01-create-order.sh
```
This creates a new order. Save the `ORDER_ID` from the response.

#### 2. Create UPI Payment
```bash
export ORDER_ID="order_xxx"
./02-create-upi-payment.sh
```

#### 3. Create Card Payment
```bash
export ORDER_ID="order_xxx"
./03-create-card-payment.sh
```

#### 4. Get Payment Details
```bash
export PAYMENT_ID="pay_xxx"
./04-get-payment.sh
```

#### 5. Verify Payment
```bash
export PAYMENT_ID="pay_xxx"
./05-verify-payment.sh
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `API_KEY` | Your merchant API key | `rpay_test_key` |
| `BASE_URL` | API base URL | `http://localhost:8080` |
| `MERCHANT_ID` | Your merchant ID | (from logs) |
| `ORDER_ID` | Order ID for payment | (from create-order) |
| `PAYMENT_ID` | Payment ID to verify | (from create-payment) |

## Test Data

All scripts use test data:
- Amount: 1000.00 INR
- Customer Email: customer@example.com
- Customer Phone: +919876543210
- Test UPI VPA: customer@upi
- Test Card: 4111111111111111 (valid Luhn)

## Expected Results

### Successful Order Creation
```json
{
  "id": "order_xxx",
  "merchantId": "merch_xxx",
  "amount": {"amount": 1000.00, "currency": "INR"},
  "status": "CREATED"
}
```

### Successful Payment Initiation
```json
{
  "success": true,
  "gatewayTransactionId": "upi_xxx",
  "status": "PENDING",
  "message": "Payment pending user action"
}
```

## Troubleshooting

### 401 Unauthorized
- Check your API key is correct
- Ensure merchant is ACTIVE status
- Verify API key is in X-Api-Key header

### 429 Too Many Requests
- Rate limit exceeded
- Wait 1 minute and retry
- Check rate limit configuration

### Order Not Found
- Verify ORDER_ID is correct
- Check order hasn't expired (15 min default)

### Payment Failed
- Check gateway is responding
- Verify payment details are valid
- Check application logs for errors
