package org.example.matcheat.domain.chat.service;

import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	// External Services or Repositories (Proposal, Request 상태 변경용)
	// private final ProposalRepository proposalRepository;
	// private final RequestRepository requestRepository;

	@Transactional
	public Long createChatRoom(Long proposalId, Long currentUserId) {
		// 1. 이미 존재하는 채팅방이 있는지 확인
		return chatRoomRepository.findByProposalId(proposalId)
				.map(ChatRoom::getId)
				.orElseGet(() -> {
					// TODO: Proposal 조회 및 권한 검증 (구매자 또는 판매자인지)
					// Proposal proposal = proposalRepository.findById(proposalId)...

					// 2. Proposal 및 Request 상태를 IN_TALK로 업데이트
					// proposal.updateStatus(ProposalStatus.IN_TALK);
					// request.updateStatus(RequestStatus.IN_TALK);

					// 3. 채팅방 생성 및 저장
					ChatRoom chatRoom = ChatRoom.create(proposalId, 1L, currentUserId, 2L);
					return chatRoomRepository.save(chatRoom).getId();
				});
	}
}