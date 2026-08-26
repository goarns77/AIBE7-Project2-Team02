package org.example.matcheat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 개발 중 API 테스트를 위해 인증은 유지하고 CSRF 검사를 비활성화하는 Security 설정
 */
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. REST API 개발을 위해 CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 요청 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 스웨거 UI 및 API Docs 관련 경로 전체 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // 개발 중인 API 경로 전체 허용 (추후 JWT 필터 적용 시 조정)
                        .requestMatchers("/api/**").permitAll()
                        // 그 외 모든 요청도 일단 허용
                        .anyRequest().permitAll()
                )

                // 3. 폼 로그인 및 기본 HTTP Basic 인증 비활성화 (HTML 로그인 창 차단)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}