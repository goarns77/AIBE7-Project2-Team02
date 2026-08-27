package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatRoomCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatRoomResponse;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
import org.example.matcheat.domain.quote.service.QuoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;

	// TODO: Proposal, Request Repository 추후 연동 예정

	/**
	 * 채팅방 생성 (단순 문의 및 제안 기반 문의 모두 대응)
	 */
	@Transactional
	public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long currentUserId) {

		// 1. Proposal 기반 진입인 경우 이미 존재하는 채팅방이 있는지 확인
		if (request.getProposalId() != null) {
			ChatRoom existingRoom = chatRoomRepository.findByProposalId(request.getProposalId())
					.orElse(null);

			if (existingRoom != null) {
				return ChatRoomResponse.from(existingRoom);
			}

			// TODO: Proposal 조회 및 권한 검증 (구매자 또는 판매자인지)
			// TODO: Proposal 및 Request 상태 변경 (추후 구현)
		}

		// 2. ChatRoom 생성 (Quote 생성을 강제하지 않고 순수 채팅방만 생성)
		// ChatRoom.builder() 또는 ChatRoom.createInquiry() 등의 팩토리 메서드 활용
		ChatRoom chatRoom = ChatRoom.builder()
				.proposalId(request.getProposalId())
				.quoteId(null) // 초기 생성 시 견적서는 null
				.originType(request.getOriginType() != null ? request.getOriginType() : ChatRoom.OriginType.INQUIRY)
				.buyerId(currentUserId)
				.sellerId(request.getSellerId())
				.build();

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

		return ChatRoomResponse.from(savedChatRoom);
	}

	/**
	 * 단건 채팅방 조회
	 */
	@Transactional(readOnly = true)
	public ChatRoomResponse getChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));

		return ChatRoomResponse.from(chatRoom);
	}
}