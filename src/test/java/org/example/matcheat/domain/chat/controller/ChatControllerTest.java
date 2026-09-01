package org.example.matcheat.domain.chat.controller;

import org.example.matcheat.domain.chat.dto.ChatRoomCreateRequest;
import org.example.matcheat.domain.chat.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChatControllerTest {
    private final ChatService chatService = mock(ChatService.class);
    private final ChatController controller = new ChatController(chatService);

    @Test
    void passesJwtSubjectAsCurrentUser() {
        ChatRoomCreateRequest request = new ChatRoomCreateRequest();

        controller.createChatRoom(jwt("42"), request);

        verify(chatService).createChatRoom(request, 42L);
    }

    private static Jwt jwt(String subject) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
    }
}
