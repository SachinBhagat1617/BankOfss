package com.ofss.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Autowired
    private CookieServerBearerTokenConverter cookieTokenConverter;
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrfSpec -> csrfSpec.disable())
                .authorizeExchange(exchange->exchange
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .pathMatchers("/public/**").hasAnyRole("USER","ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2->
                        oauth2
                                .bearerTokenConverter(cookieTokenConverter)
                                .jwt(jwt->
                                jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                )
                .build();
            // in oauth2ResourceServer, we extract the token from cookie and then set it to header
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter=new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter( jwt->{
            // Extract roles from the "roles" claim

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            // {OAuth2-PKCE={roles=[USER]},account={roles=[manage-account,manage-account-links, view-profile]}}
            List<String> roles=null;
            for(Map.Entry<String,Object> entry:resourceAccess.entrySet()){
                if(entry.getKey().equals("OAuth2-PKCE")){
                    Map<String,List<String>> inner=(Map<String,List<String>>)entry.getValue();
                    roles=inner.get("roles");
                    System.out.println("Roles extracted: "+roles);
                }
            }
            return Flux.fromIterable(roles)
                    .map(role->new SimpleGrantedAuthority("ROLE_"+role));

        });
        return jwtAuthenticationConverter;
    }

}
