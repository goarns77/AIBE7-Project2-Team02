package org.example.matcheat.domain.chat.repository;

import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	Optional<ChatRoom> findByProposalId(Long proposalId);
}