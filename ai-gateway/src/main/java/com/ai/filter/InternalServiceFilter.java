package com.ai.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;


/**
 * @author 潘越
 */
@Component
public class InternalServiceFilter implements WebFilter {
    private final Pattern INTERNAL_SERVICE_PATTERN = Pattern.compile(".*/internal-service/.*");

    @Override
    public Mono<Void> filter(ServerWebExchange ctx, WebFilterChain chain) {
        ServerHttpRequest request = ctx.getRequest();
        String path = request.getPath().value();
        ServerHttpResponse response = ctx.getResponse();
        if (INTERNAL_SERVICE_PATTERN.matcher(path).matches()) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        return chain.filter(ctx);
    }
}
