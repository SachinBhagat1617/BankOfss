package com.ofss.gateway.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CookieServerBearerTokenConverter extends ServerBearerTokenAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return extractToken(exchange)
                .flatMap(token -> {
                    // Modified exchange banao with Authorization header
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("Authorization", "Bearer " + token)
                                    .build())
                            .build();

                    // Parent class ko modified exchange pass karo
                    return super.convert(modifiedExchange);
                })
                .switchIfEmpty(super.convert(exchange));
    }

    private Mono<String> extractToken(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst("access_token"))
                .map(cookie -> cookie.getValue());
    }
}