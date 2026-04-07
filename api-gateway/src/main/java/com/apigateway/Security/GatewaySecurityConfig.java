package com.apigateway.Security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
@Slf4j
@Component
public class GatewaySecurityConfig implements GlobalFilter, Ordered {
    @Autowired
    private JwtUtil jwtUtil;

    private final List<String> PUBLIC_PATHS=List.of(
            "/payment/user/create",
            "/payment/user/login"
    );
    private boolean isPublicPth(String path){
        log.info("In public path {}",path);
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path= exchange.getRequest().getPath().value();
        if(isPublicPth(path)){
           return chain.filter(exchange);
        }
        String auth=exchange.getRequest().getHeaders().getFirst("Authorization");
        if(auth==null|| !auth.startsWith("Bearer ")){
            log.warn("Missing or malformed Authorization header for path {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        try {
            String token = auth.substring(7);
            if (jwtUtil.verifyExpiry(token)) {
                Claims claims = jwtUtil.validateAndParse(token);
                String email = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);
                ServerWebExchange mutatedExchange = exchange.mutate().request(r ->
                                r.header("X-User-Email", email)
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-User-Role", role))
                        .build();
                return chain.filter(mutatedExchange);

            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } catch (Exception e) {
            log.warn("Invalid JWT for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
          return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 96;
    }
}
