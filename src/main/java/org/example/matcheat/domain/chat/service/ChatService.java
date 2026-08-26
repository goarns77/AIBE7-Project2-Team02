package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
import org.example.matcheat.domain.quote.service.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final QuoteService quoteService;

	// TODO: Proposal, Request Repository 추후 연동 예정

	@Transactional
	public Long createChatRoom(Long proposalId, Long currentUserId) {
		// 1. 이미 존재하는 채팅방이 있는지 확인
		return chatRoomRepository.findByProposalId(proposalId)
				.map(ChatRoom::getId)
				.orElseGet(() -> {
					// TODO: Proposal 조회 및 권한 검증 (구매자 또는 판매자인지)

					// 2. Proposal 및 Request 상태 변경 (추후 구현)

					// 3. 채팅방 생성 및 저장
					ChatRoom chatRoom = ChatRoom.create(proposalId, 1L, currentUserId, 2L);
					ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

					// 4. 1차 견적서(Quote) 자동 생성 (Proposal 데이터는 임시 하드코딩 상태)
					quoteService.createPrimaryQuoteFromProposal(savedChatRoom.getId(), proposalId, currentUserId);

					return savedChatRoom.getId();
				});
	}
}