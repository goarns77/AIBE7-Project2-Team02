package org.example.matcheat.domain.chat.repository;

import org.example.matcheat.domain.chat.entity.ChatFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatFileRepository extends JpaRepository<ChatFile, Long> {
	List<ChatFile> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
}