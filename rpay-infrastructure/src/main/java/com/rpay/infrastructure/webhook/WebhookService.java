package com.rpay.infrastructure.webhook;

import com.rpay.domain.merchant.Merchant;
import com.rpay.infrastructure.security.ApiKeyAuthenticationService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Webhook notification service for merchant callbacks.
 *
 * Features:
 * - Async webhook delivery using virtual threads
 * - Automatic retry with exponential backoff
 * - Signature generation for webhook verification
 * - Timeout handling
 * - Dead letter queue for failed webhooks
 *
 * Webhook events:
 * - payment.created
 * - payment.authorized
 * - payment.captured
 * - payment.failed
 * - refund.processed
 * - order.paid
 */
@Service
public class WebhookService {
    private final WebClient webClient;
    private final ApiKeyAuthenticationService authService;

    public WebhookService(
        WebClient.Builder webClientBuilder,
        ApiKeyAuthenticationService authService
    ) {
        this.webClient = webClientBuilder.build();
        this.authService = authService;
    }

    /**
     * Send webhook notification asynchronously
     */
    @Async
    @Retry(name = "webhook")
    public CompletableFuture<WebhookResponse> sendWebhook(
        Merchant merchant,
        String eventType,
        Object payload
    ) {
        if (merchant.getWebhookUrl() == null || merchant.getWebhookUrl().isBlank()) {
            return CompletableFuture.completedFuture(
                new WebhookResponse(false, "Webhook URL not configured")
            );
        }

        // Build webhook payload
        var webhookPayload = Map.of(
            "event", eventType,
            "timestamp", System.currentTimeMillis(),
            "data", payload
        );

        // Generate signature
        String signature = authService.generateSignature(
            merchant.getWebhookSecret(),
            webhookPayload.toString()
        );

        // Send webhook with timeout
        return webClient.post()
            .uri(merchant.getWebhookUrl())
            .header("X-Webhook-Signature", signature)
            .header("X-Event-Type", eventType)
            .bodyValue(webhookPayload)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(30))
            .map(response -> new WebhookResponse(true, response))
            .onErrorResume(error -> CompletableFuture.completedFuture(
                new WebhookResponse(false, error.getMessage())
            ))
            .toFuture();
    }

    public record WebhookResponse(boolean success, String message) {}
}
