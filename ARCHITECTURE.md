# RPay Architecture Documentation

## System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
│              (Web, Mobile, Backend Integrations)            │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS + API Key + Signature
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    API Gateway Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Auth Filter  │→ │ Rate Limiter │→ │ Idempotency  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  Application Services                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │OrderService  │  │PaymentService│  │RefundService │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────┬──────────────────┬──────────────────┬─────────────────┘
      │                  │                  │
┌─────▼──────────────────▼──────────────────▼─────────────────┐
│                     Domain Layer                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │  Order   │ │ Payment  │ │ Refund   │ │ Customer │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────┬──────────────────┬──────────────────┬─────────────────┘
      │                  │                  │
┌─────▼──────────────────▼──────────────────▼─────────────────┐
│                 Infrastructure Layer                         │
│ ┌────────────┐ ┌────────────┐ ┌────────────┐               │
│ │ PostgreSQL │ │   Redis    │ │  Gateways  │               │
│ │   (JPA)    │ │  (Cache)   │ │ (UPI/Card) │               │
│ └────────────┘ └────────────┘ └────────────┘               │
└──────────────────────────────────────────────────────────────┘
```

## Module Architecture (Hexagonal/Clean)

### rpay-domain (Core Business Logic)
**Responsibility**: Pure business logic, no dependencies
- **Entities**: Order, Payment, Customer, Merchant, Refund
- **Value Objects**: Money, PaymentId, OrderId
- **Ports**: Repository interfaces, Gateway interfaces
- **Business Rules**: State transitions, validation

### rpay-application (Use Cases)
**Responsibility**: Orchestrate business logic
- **Services**: OrderService, PaymentService
- **DTOs**: CreateOrderRequest, CreatePaymentRequest
- **Orchestration**: Transaction management, workflow coordination

### rpay-infrastructure (External Integrations)
**Responsibility**: Implement ports, integrate external systems
- **Persistence**: JPA repositories, database entities
- **Gateways**: UPI, Card, NetBanking, Wallet implementations
- **Cache**: Redis integration
- **Security**: API key auth, signature verification
- **Idempotency**: Distributed idempotency
- **Rate Limiting**: Token bucket via Redis
- **Webhooks**: Async notification delivery

### rpay-api (Entry Point)
**Responsibility**: REST API, request/response handling
- **Controllers**: OrderController, PaymentController
- **Filters**: Authentication, rate limiting
- **Configuration**: Spring Boot setup
- **Monitoring**: Actuator, Prometheus metrics

## Design Patterns

### 1. Hexagonal Architecture (Ports & Adapters)
**Why**: Isolate business logic from infrastructure concerns

**Ports**:
- `PaymentRepository`: Domain interface
- `PaymentGateway`: Domain interface

**Adapters**:
- `PaymentRepositoryImpl`: JPA implementation
- `UpiGateway`: UPI implementation
- `CardGateway`: Card implementation

### 2. Strategy Pattern
**Where**: Payment gateway selection
```java
Map<PaymentMethod, PaymentGateway> gatewayRegistry;

PaymentGateway gateway = gatewayRegistry.get(paymentMethod);
gateway.initiatePayment(payment, details);
```

### 3. Repository Pattern
**Where**: Data access abstraction
```java
public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
}
```

### 4. Builder Pattern
**Where**: Complex object construction
```java
Payment payment = Payment.builder()
    .merchantId(merchantId)
    .orderId(orderId)
    .amount(amount)
    .build();
```

### 5. Circuit Breaker Pattern
**Where**: External gateway calls
```java
@CircuitBreaker(name = "upiGateway")
public PaymentGatewayResponse initiatePayment(...) {
    // Call to external UPI gateway
}
```

## Performance Architecture

### Concurrency Model (Java 21 Virtual Threads)
```
Traditional Threads:
- 1 thread = 1MB stack
- 10,000 threads = 10GB memory
- Thread pooling required

Virtual Threads (Java 21):
- 1 virtual thread = ~1KB
- 10,000,000 virtual threads = 10GB memory
- No thread pooling needed
- Scales to millions of concurrent requests
```

### Caching Strategy
```
Level 1: In-memory (Caffeine)
- Payment objects (5 min TTL)
- Order objects (5 min TTL)

Level 2: Distributed (Redis)
- Idempotency keys (24 hours)
- API key lookups (1 hour)
- Rate limit counters (1 minute)
```

### Database Optimization
```sql
-- Strategic indexes for high-performance queries
CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_merchant ON payments(merchant_id);
CREATE INDEX idx_payment_gateway_txn ON payments(gateway_transaction_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_created ON payments(created_at);
```

## Security Architecture

### Authentication Flow
```
1. Client sends request with X-Api-Key header
2. AuthenticationFilter extracts API key
3. ApiKeyAuthenticationService validates key
4. Merchant authenticated and stored in request context
5. Rate limits checked
6. Request proceeds to controller
```

### Signature Verification
```
1. Client generates HMAC-SHA256(request_body, api_secret)
2. Sends signature in X-Signature header
3. Server regenerates signature
4. Constant-time comparison to prevent timing attacks
```

### Rate Limiting (Token Bucket)
```
Bucket Configuration:
- Merchant: 1000 tokens/minute
- IP: 100 tokens/minute
- Payment creation: 10 tokens/minute

Algorithm:
1. Request arrives
2. Check if tokens available
3. If yes: consume token, allow request
4. If no: return 429 Too Many Requests
5. Tokens refill at configured rate
```

## Data Flow

### Payment Creation Flow
```
1. POST /api/v1/payments
   └─> AuthenticationFilter (auth + rate limit)
       └─> PaymentController.createPayment()
           └─> IdempotencyService.check()
               └─> PaymentService.createPayment()
                   ├─> OrderService.validateOrder()
                   ├─> PaymentRepository.save()
                   ├─> PaymentGateway.initiatePayment()
                   └─> WebhookService.sendWebhook()
```

### Payment State Machine
```
        ┌─────────┐
        │ CREATED │
        └────┬────┘
             │
        ┌────▼────────┐
        │ PROCESSING  │
        └─┬─────────┬─┘
          │         │
    ┌─────▼──┐  ┌──▼────────┐
    │PENDING │  │  FAILED   │
    │  AUTH  │  │ (terminal)│
    └─┬──────┘  └───────────┘
      │
┌─────▼──────┐
│ AUTHORIZED │
└─────┬──────┘
      │
  ┌───▼────┐
  │CAPTURED│
  └───┬────┘
      │
  ┌───▼────┐
  │REFUNDED│
  └────────┘
```

## Scalability Strategy

### Horizontal Scaling
- **Stateless Design**: No session state on application servers
- **Distributed Cache**: Redis for shared state (idempotency, rate limits)
- **Load Balancing**: Round-robin across multiple instances
- **Database Connection Pooling**: HikariCP with optimal settings

### Vertical Scaling
- **Virtual Threads**: 10M+ concurrent connections per server
- **JVM Tuning**: G1GC with large heap sizes
- **Connection Pooling**: Reuse database connections

### India-Scale Capacity Planning
```
Peak Traffic Assumptions:
- 10 million concurrent users
- 100,000 payments/second
- 1 million merchants

Infrastructure:
- 100 application servers (with virtual threads)
- PostgreSQL with read replicas
- Redis cluster (sharded)
- CDN for static content
- Multi-region deployment
```

## Monitoring & Observability

### Metrics Collected
1. **Business Metrics**
   - Payments created/minute
   - Success rate by payment method
   - Average transaction value
   - Refund rate

2. **Technical Metrics**
   - API response time (p50, p95, p99)
   - Database query time
   - Cache hit rate
   - Circuit breaker states
   - Rate limit hits

3. **Infrastructure Metrics**
   - CPU/Memory usage
   - Virtual thread count
   - Database connection pool
   - Redis memory usage

### Health Checks
```
/actuator/health - Overall health
/actuator/health/db - Database connectivity
/actuator/health/redis - Redis connectivity
/actuator/health/gateways - Payment gateways status
```

## Deployment Architecture

### Development
```
docker-compose up
- PostgreSQL
- Redis
- Prometheus
- Grafana
```

### Production
```
Kubernetes Deployment:
- Multiple pods with autoscaling
- PostgreSQL managed service (RDS/Cloud SQL)
- Redis cluster (ElastiCache/Memory Store)
- Load balancer (ALB/GCP LB)
- Service mesh for traffic management
```

## Future Enhancements

1. **Event Sourcing**: Store all payment events for audit trail
2. **CQRS**: Separate read/write models for optimization
3. **GraphQL API**: More flexible querying
4. **gRPC**: High-performance internal communication
5. **Kafka**: Event streaming for analytics
6. **Machine Learning**: Fraud detection
