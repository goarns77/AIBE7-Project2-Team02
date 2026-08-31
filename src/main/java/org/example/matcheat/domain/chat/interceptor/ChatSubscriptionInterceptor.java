package org.example.matcheat.domain.chat.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.service.ChatService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [신규] STOMP SUBSCRIBE 시점에 채팅방 참여자인지 검증한다.
 *
 * 기존에는 발행(SEND)과 이력 조회(REST)만 참여자 검증이 있었고,
 * 구독(SUBSCRIBE) 자체는 아무 검증 없이 누구나 chatRoomId만 알면
 * 실시간 메시지를 몰래 받아볼 수 있었다 — 웹소켓 테스트에서 실제로 확인된 문제.
 *
 * currentUserId 결정 방식은 HTTP 컨트롤러들의 resolveCurrentUserId()와
 * 동일한 임시 패턴이다. 인증이 붙으면 resolveCurrentUserId(accessor) 내부만
 * STOMP CONNECT 시 세팅된 Principal/세션 속성에서 유도하도록 교체하면 된다.
 *
 * [로깅 참고] preSend에서 던진 예외는 Spring이 내부적으로 잡아 STOMP ERROR
 * 프레임으로 클라이언트에 전달하는데, 이 과정 자체는 기본 로그 레벨(INFO)에서는
 * 콘솔에 스택트레이스로 안 남는다. 그래서 거부 사유를 직접 로그로 남긴 뒤 다시
 * 던진다 — 클라이언트로 가는 STOMP ERROR 동작은 그대로 유지된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSubscriptionInterceptor implements ChannelInterceptor {

	private static final Pattern CHAT_ROOM_DESTINATION_PATTERN = Pattern.compile("^/sub/chat/room/(\\d+)$");

	private final ChatService chatService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			Long chatRoomId = extractChatRoomId(accessor.getDestination());

			if (chatRoomId != null) {
				Long currentUserId = resolveCurrentUserId(accessor);

				try {
					ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
					chatRoom.validateParticipant(currentUserId);
				} catch (IllegalArgumentException e) {
					// 존재하지 않는 방이거나 참여자가 아닌 경우 여기로 온다.
					log.warn("[STOMP SUBSCRIBE 거부] chatRoomId={}, userId={}, 사유={}",
							chatRoomId, currentUserId, e.getMessage());
					throw e; // 그대로 다시 던져야 STOMP ERROR 프레임이 클라이언트로 간다.
				}
			}
		}

		return message;
	}

	private Long extractChatRoomId(String destination) {
		if (destination == null) {
			return null;
		}
		Matcher matcher = CHAT_ROOM_DESTINATION_PATTERN.matcher(destination);
		return matcher.matches() ? Long.parseLong(matcher.group(1)) : null;
	}

	private Long resolveCurrentUserId(StompHeaderAccessor accessor) {
		// TODO: SecurityContext/JWT 적용 시, STOMP CONNECT 단계에서 세션에 심어둔
		// 인증된 사용자 정보(accessor.getUser() 등)에서 유도하도록 교체.
		// 지금은 다른 컨트롤러들의 resolveCurrentUserId()와 동일한 임시 고정값.
		return 1L;
	}
}