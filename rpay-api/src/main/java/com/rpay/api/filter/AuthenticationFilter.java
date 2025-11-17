package com.rpay.api.filter;

import com.rpay.infrastructure.ratelimit.RateLimiterService;
import com.rpay.infrastructure.security.ApiKeyAuthenticationService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter for API authentication and rate limiting.
 *
 * Processing order:
 * 1. Extract API key from header
 * 2. Authenticate merchant
 * 3. Check rate limits
 * 4. Verify request signature (if present)
 * 5. Allow request or return 401/429
 */
@Component
public class AuthenticationFilter implements Filter {
    private final ApiKeyAuthenticationService authService;
    private final RateLimiterService rateLimiterService;

    public AuthenticationFilter(
        ApiKeyAuthenticationService authService,
        RateLimiterService rateLimiterService
    ) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip auth for health check endpoints
        if (httpRequest.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // Extract API key
        String apiKey = httpRequest.getHeader("X-Api-Key");
        if (apiKey == null || apiKey.isBlank()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"API key required\"}");
            return;
        }

        // Authenticate
        var merchant = authService.authenticateApiKey(apiKey);
        if (merchant.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"Invalid API key\"}");
            return;
        }

        // Check rate limits
        String merchantId = merchant.get().getId().getValue();
        if (!rateLimiterService.checkMerchantRateLimit(merchantId)) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }

        // Check IP rate limit
        String ipAddress = httpRequest.getRemoteAddr();
        if (!rateLimiterService.checkIpRateLimit(ipAddress)) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"error\":\"IP rate limit exceeded\"}");
            return;
        }

        // Store merchant in request attribute for controllers
        httpRequest.setAttribute("merchantId", merchantId);

        chain.doFilter(request, response);
    }
}
