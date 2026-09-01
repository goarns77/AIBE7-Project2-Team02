package org.example.matcheat.domain.chat.interceptor;

import org.example.matcheat.domain.account.service.TradeAccountValidationService;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatSubscriptionInterceptor implements ChannelInterceptor {
    private static final Pattern CHAT_ROOM_DESTINATION = Pattern.compile("^/sub/chat/room/(\\d+)$");
    private static final String BEARER_PREFIX = "Bearer ";

    private final ChatService chatService;
    private final TradeAccountValidationService accounts;
    private final JwtDecoder jwtDecoder;
    private final Converter<Jwt, AbstractAuthenticationToken> authenticationConverter;

    public ChatSubscriptionInterceptor(
            ChatService chatService,
            TradeAccountValidationService accounts,
            @Qualifier("accountJwtDecoder") JwtDecoder jwtDecoder,
            @Qualifier("accountJwtAuthenticationConverter")
            Converter<Jwt, AbstractAuthenticationToken> authenticationConverter) {
        this.chatService = chatService;
        this.accounts = accounts;
        this.jwtDecoder = jwtDecoder;
        this.authenticationConverter = authenticationConverter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(authenticate(accessor));
            return message;
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Long chatRoomId = extractChatRoomId(accessor.getDestination());
            if (chatRoomId != null) {
                ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
                Long userId = currentUserId(accessor);
                chatRoom.validateParticipant(userId, accounts.sellerIdForUserOrNull(userId));
            }
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("STOMP CONNECT requires a Bearer access token.");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new MessagingException("STOMP CONNECT requires a Bearer access token.");
        }
        try {
            AbstractAuthenticationToken authentication = authenticationConverter.convert(jwtDecoder.decode(token));
            if (authentication == null) {
                throw new MessagingException("Unable to create STOMP authentication.");
            }
            return authentication;
        } catch (RuntimeException exception) {
            throw new MessagingException("Invalid STOMP access token.", exception);
        }
    }

    private static Long currentUserId(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new MessagingException("Unauthenticated STOMP session.");
        }
        try {
            return Long.valueOf(accessor.getUser().getName());
        } catch (NumberFormatException exception) {
            throw new MessagingException("Invalid authenticated account id.", exception);
        }
    }

    private static Long extractChatRoomId(String destination) {
        if (destination == null) {
            return null;
        }
        Matcher matcher = CHAT_ROOM_DESTINATION.matcher(destination);
        return matcher.matches() ? Long.valueOf(matcher.group(1)) : null;
    }
}
