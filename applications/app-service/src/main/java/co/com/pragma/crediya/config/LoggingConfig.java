package co.com.pragma.crediya.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class LoggingConfig {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private static final Logger log = LoggerFactory.getLogger(LoggingConfig.class);

    /**
     * WebFilter that ensures every request has a correlation id, stores it in MDC, propagates it via Reactor Context,
     * and echoes it back in the response header. This lives in the entry-point (infrastructure) layer to keep domain clean.
     */
    @Bean
    public WebFilter correlationIdWebFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            String incoming = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
            String correlationId = Optional.ofNullable(incoming)
                    .filter(s -> !s.isBlank())
                    .orElse(UUID.randomUUID().toString());

            // Add header to response for downstream visibility
            response.getHeaders().set(CORRELATION_ID_HEADER, correlationId);

            // Put into MDC for log statements on this thread
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            if (log.isDebugEnabled()) {
                log.debug("Incoming {} {} - correlationId={}", request.getMethod(), request.getURI().getPath(), correlationId);
            }


            // 2) Continúo la cadena guardando el cid en el Reactor Context
            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put(CORRELATION_ID_MDC_KEY, correlationId));

        };
    }
}
