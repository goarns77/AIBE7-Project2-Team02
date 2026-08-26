package org.example.matcheat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// ws://localhost:8080/ws-stomp 연결 엔드포인트 설정
		registry.addEndpoint("/ws-stomp")
				.setAllowedOriginPatterns("*"); // CORS 허용
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지 구독 요청 prefix : /sub (메시지를 받을 때)
		registry.enableSimpleBroker("/sub");

		// 메시지 발행 요청 prefix : /pub (메시지를 보낼 때)
		registry.setApplicationDestinationPrefixes("/pub");
	}
}