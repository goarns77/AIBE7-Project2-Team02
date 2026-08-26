package org.example.matcheat.domain.chat.repository;

import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	// 채팅방 이전 메시지 내역 조회용
	List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}