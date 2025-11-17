# RPay Local Testing Guide

Complete guide to test the payment gateway locally, from setup to full payment flow.

## 📋 Prerequisites

### Required Software
- Java 21 (JDK)
- Maven 3.8+
- Docker & Docker Compose
- curl (for API testing)
- jq (optional, for JSON formatting)

### Verify Installation
```bash
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.8+
docker --version
docker-compose --version
```

## 🚀 Step-by-Step Setup

### Step 1: Start Infrastructure

Start PostgreSQL, Redis, Prometheus, and Grafana:

```bash
cd /home/user/rpay
docker-compose up -d
```

Verify services are running:
```bash
docker-compose ps
```

You should see:
- `rpay-postgres` (port 5432)
- `rpay-redis` (port 6379)
- `rpay-prometheus` (port 9090)
- `rpay-grafana` (port 3000)

### Step 2: Build the Application

```bash
cd /home/user/rpay
mvn clean install
```

This will:
- Compile all modules
- Run tests (if any)
- Create JAR files

Expected output: `BUILD SUCCESS`

### Step 3: Start the Application

```bash
cd rpay-api
mvn spring-boot:run
```

**IMPORTANT**: Watch the console output! You'll see:

```
================================================================================
TEST MERCHANT CREATED SUCCESSFULLY!
================================================================================
Merchant ID:    merch_xxxxxxxxxx
Business Name:  Test Merchant
Email:          test@merchant.com
Status:         ACTIVE

API KEY (save this for testing):
  rpay_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

Use this API key in the X-Api-Key header for all requests
================================================================================
```

**COPY AND SAVE** the API Key and Merchant ID!

### Step 4: Verify Application is Running

Check health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## 🧪 Testing the Complete Payment Flow

### Method 1: Using Test Scripts (Recommended)

#### Setup Environment Variables
```bash
export API_KEY="rpay_your_key_from_logs"
export MERCHANT_ID="merch_your_id_from_logs"
```

#### Run Complete Test Flow
```bash
cd /home/user/rpay/test-scripts
chmod +x *.sh
./run-all-tests.sh
```

### Method 2: Manual Testing with cURL

#### Test 1: Create an Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -d '{
    "merchantId": "YOUR_MERCHANT_ID",
    "amount": 1000.00,
    "currency": "INR",
    "customerEmail": "customer@example.com",
    "customerPhone": "+919876543210",
    "receipt": "order_rcpt_001",
    "notes": "Test order"
  }'
```

**Expected Response:**
```json
{
  "id": "order_abc123",
  "merchantId": "merch_xyz",
  "customerId": "cust_def456",
  "amount": {
    "amount": 1000.00,
    "currency": "INR"
  },
  "receipt": "order_rcpt_001",
  "notes": "Test order",
  "status": "CREATED",
  "expiresAt": "2025-01-17T18:30:00Z",
  "paymentAttempts": 0,
  "createdAt": "2025-01-17T18:15:00Z"
}
```

**Save the `order_id` for next steps!**

#### Test 2: Create a UPI Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: unique_$(date +%s)" \
  -d '{
    "orderId": "order_abc123",
    "paymentMethod": "upi",
    "paymentDetails": {
      "vpa": "customer@upi",
      "flow": "collect"
    }
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "gatewayTransactionId": "upi_xyz789",
  "status": "PENDING",
  "message": "Payment pending user action",
  "redirectUrl": "upi://pay?pa=customer@upi"
}
```

#### Test 3: Create a Card Payment

First create a new order (orders are single-use), then:

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: unique_$(date +%s)" \
  -d '{
    "orderId": "order_new_123",
    "paymentMethod": "card",
    "paymentDetails": {
      "cardNumber": "4111111111111111",
      "cardholderName": "Test User",
      "expiryMonth": "12",
      "expiryYear": "2025",
      "cvv": "123",
      "cardType": "CREDIT"
    }
  }'
```

**Expected Response (3DS required):**
```json
{
  "success": true,
  "gatewayTransactionId": "card_abc456",
  "status": "PENDING",
  "message": "Payment pending user action",
  "redirectUrl": "https://3ds.example.com/auth?txn=..."
}
```

#### Test 4: Create Net Banking Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: unique_$(date +%s)" \
  -d '{
    "orderId": "order_another_123",
    "paymentMethod": "netbanking",
    "paymentDetails": {
      "bankCode": "HDFC"
    }
  }'
```

#### Test 5: Create Wallet Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: unique_$(date +%s)" \
  -d '{
    "orderId": "order_wallet_123",
    "paymentMethod": "wallet",
    "paymentDetails": {
      "walletProvider": "paytm",
      "phone": "+919876543210"
    }
  }'
```

#### Test 6: Get Payment Details

```bash
curl http://localhost:8080/api/v1/payments/pay_abc123 \
  -H "X-Api-Key: YOUR_API_KEY"
```

#### Test 7: Verify Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments/pay_abc123/verify \
  -H "X-Api-Key: YOUR_API_KEY"
```

#### Test 8: Get Order Details

```bash
curl http://localhost:8080/api/v1/orders/order_abc123 \
  -H "X-Api-Key: YOUR_API_KEY"
```

## 🔍 Testing Advanced Features

### Idempotency Testing

Send the same request twice with same idempotency key:

```bash
# First request
curl -X POST http://localhost:8080/api/v1/payments \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: test_idem_key_123" \
  -d '{ ... }'

# Second request (should return cached response)
curl -X POST http://localhost:8080/api/v1/payments \
  -H "X-Api-Key: YOUR_API_KEY" \
  -H "X-Idempotency-Key: test_idem_key_123" \
  -d '{ ... }'
```

Both should return identical responses!

### Rate Limiting Testing

Send many requests quickly:

```bash
for i in {1..15}; do
  curl -X POST http://localhost:8080/api/v1/payments/pay_test/verify \
    -H "X-Api-Key: YOUR_API_KEY"
  echo ""
done
```

After the limit (10 requests/minute), you should see:
```json
{"error":"Rate limit exceeded"}
```

### Invalid API Key Testing

```bash
curl http://localhost:8080/api/v1/orders/order_test \
  -H "X-Api-Key: invalid_key"
```

Expected:
```json
{"error":"Invalid API key"}
```

### Missing API Key Testing

```bash
curl http://localhost:8080/api/v1/orders/order_test
```

Expected:
```json
{"error":"API key required"}
```

## 📊 Monitoring & Metrics

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```

### Prometheus Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

### View Metrics in Prometheus

Open browser: http://localhost:9090

Example queries:
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Total request time
- `jvm_memory_used_bytes` - Memory usage

### View Dashboards in Grafana

1. Open browser: http://localhost:3000
2. Login: admin/admin
3. Add Prometheus datasource: http://prometheus:9090
4. Create dashboards for RPay metrics

## 🐛 Troubleshooting

### Application won't start

**Error:** `Port 8080 already in use`
```bash
# Find process using port 8080
lsof -i :8080
# Kill it
kill -9 <PID>
```

**Error:** `Cannot connect to PostgreSQL`
```bash
# Check if PostgreSQL is running
docker-compose ps
# Restart if needed
docker-compose restart postgres
```

**Error:** `Cannot connect to Redis`
```bash
# Check Redis
docker-compose ps
# Restart if needed
docker-compose restart redis
```

### Database issues

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d
# Restart application to recreate schema
```

### No test merchant created

Check application logs for errors. Manually create merchant if needed:
```sql
-- Connect to database
docker exec -it rpay-postgres psql -U rpay -d rpaydb

-- Insert test merchant
INSERT INTO merchants (id, business_name, email, phone, status, api_key, created_at, updated_at, version)
VALUES (
  'merch_test_123',
  'Test Merchant',
  'test@merchant.com',
  '+919876543210',
  'ACTIVE',
  'hashed_api_key_here',
  NOW(),
  NOW(),
  0
);
```

### Payment always fails

This is a **mock implementation**. Actual payment gateways require:
1. Gateway API credentials
2. Network connectivity to gateway servers
3. Proper webhook callback URLs

Current implementation simulates gateway responses for testing.

## ✅ Success Criteria

You've successfully tested RPay if:

- ✅ Application starts without errors
- ✅ Test merchant is created with API key
- ✅ Can create orders via API
- ✅ Can initiate payments (UPI, Card, NetBanking, Wallet)
- ✅ Payment status changes appropriately
- ✅ Idempotency works (same key = same response)
- ✅ Rate limiting blocks excessive requests
- ✅ Invalid API keys are rejected
- ✅ Health checks return UP status
- ✅ Metrics are available in Prometheus

## 📝 Test Checklist

- [ ] Docker services running
- [ ] Application started successfully
- [ ] Test merchant created
- [ ] API key saved
- [ ] Create order - SUCCESS
- [ ] Create UPI payment - SUCCESS
- [ ] Create Card payment - SUCCESS
- [ ] Create NetBanking payment - SUCCESS
- [ ] Create Wallet payment - SUCCESS
- [ ] Get payment details - SUCCESS
- [ ] Verify payment - SUCCESS
- [ ] Get order details - SUCCESS
- [ ] Test idempotency - SUCCESS
- [ ] Test rate limiting - SUCCESS
- [ ] Test invalid API key - REJECTED
- [ ] View Prometheus metrics - SUCCESS
- [ ] View Grafana dashboards - SUCCESS

## 🎯 Next Steps

After successful local testing:

1. **Integration Testing**: Integrate with actual payment gateways
2. **Load Testing**: Use tools like JMeter or Gatling
3. **Security Testing**: Penetration testing, security audit
4. **Performance Testing**: Measure throughput and latency
5. **Production Deployment**: Deploy to cloud infrastructure

## 📚 Additional Resources

- API Documentation: See README.md
- Architecture Details: See ARCHITECTURE.md
- Source Code: Explore the 4-layer modular structure
- Metrics: http://localhost:9090 (Prometheus)
- Dashboards: http://localhost:3000 (Grafana)
