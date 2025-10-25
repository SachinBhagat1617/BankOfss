package com.ofss.gateway.config;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ACCOUNT", r -> r
                        .path("/accounts/**")
                        .uri("lb://ACCOUNT"))
                .route("CUSTOMER",r->r
                        .path("/customers/**")
                        .uri("lb://CUSTOMER")
                )
                .route("KYCSERVICE",r->r
                        .path("/kyc/**")
                        .uri("lb://KYCSERVICE")
                )
                .route("eureka-server", r -> r
                        .path("/eureka/**")
                        .filters(f -> f.rewritePath("/eureka/?(?<segment>.*)", "/${segment}"))
                        .uri("http://localhost:8761"))
                // Catch-all route for other Eureka resources
                .route("eureka-resources", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8761"))
                .build();
    }
}