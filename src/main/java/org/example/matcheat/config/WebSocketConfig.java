package org.example.matcheat.config;

import org.example.matcheat.domain.chat.interceptor.ChatSubscriptionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final ChatSubscriptionInterceptor chatSubscriptionInterceptor;
	private final String[] allowedOriginPatterns;

	public WebSocketConfig(
			ChatSubscriptionInterceptor chatSubscriptionInterceptor,
			@Value("${app.websocket.allowed-origin-patterns:http://localhost:*}") String allowedOriginPatterns) {
		this.chatSubscriptionInterceptor = chatSubscriptionInterceptor;
		this.allowedOriginPatterns = allowedOriginPatterns.split("\\s*,\\s*");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// [수정 P0-4] 동일 경로('/ws-stomp') 중복 등록 제거
		// SockJS 엔드포인트 하나만 유지하여 핸드셰이크 충돌 방지
		registry.addEndpoint("/ws-stomp")
				.setAllowedOriginPatterns(allowedOriginPatterns)
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지 구독 요청 prefix : /sub (메시지를 수신할 때)
		registry.enableSimpleBroker("/sub");

		// 메시지 발행 요청 prefix : /pub (메시지를 보낼 때)
		registry.setApplicationDestinationPrefixes("/pub");
	}

	// [신규] SUBSCRIBE 시점 참여자 검증 인터셉터 등록
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(chatSubscriptionInterceptor);
	}
}
