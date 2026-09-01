package org.example.matcheat.config;

import org.example.matcheat.domain.account.security.AccountSecurityErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("accountJwtDecoder") JwtDecoder jwtDecoder,
            @Qualifier("accountJwtAuthenticationConverter")
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
            AccountSecurityErrorHandler securityErrorHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/mypage/**",
                                "/requests/**",
                                "/css/**",
                                "/account/**",
                                "/order-request-test.html",
                                "/ws/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products",
                                "/api/v1/products/**",
                                "/api/v1/requests/*/proposals",
                                "/api/v1/quotes/to-buyer"
                        ).hasRole("SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/mine",
                                "/api/v1/proposals/sent",
                                "/api/v1/proposals/eligibility",
                                "/api/v1/estimates/received",
                                "/api/v1/requests",
                                "/api/v1/requests/search",
                                "/api/products/*/order-requests/recommendations"
                        ).hasRole("SELLER")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/products",
                                "/api/v1/products/search",
                                "/api/v1/products/{id}"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
