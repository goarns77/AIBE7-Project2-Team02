package org.example.matcheat.domain.chat.repository;

import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	Optional<ChatRoom> findByProposalId(Long proposalId);

	// [P1-6 추가] 동일 구매자-판매자 간 활성화된 INQUIRY 채팅방 조회
	Optional<ChatRoom> findByBuyerIdAndSellerIdAndOriginTypeAndStatus(
			Long buyerId,
			Long sellerId,
			ChatRoom.OriginType originType,
			ChatRoom.Status status
	);
}