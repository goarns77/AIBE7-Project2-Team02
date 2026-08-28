package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatRoomCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatRoomResponse;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;

	/**
	 * 외부 컨트롤러 요청을 통한 채팅방 생성 (단순 문의 및 제안 기반 문의 모두 대응)
	 */
	@Transactional
	public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long currentUserId) {
		ChatRoom chatRoom = getOrCreateChatRoomEntity(
				request.getProposalId(),
				request.getOriginType(),
				currentUserId,
				request.getSellerId()
		);
		return ChatRoomResponse.from(chatRoom);
	}

	/**
	 * [P0-1] 타 서비스(QuoteService 등) 내부 전용: 채팅방을 생성하거나 기존 방을 조회하여 엔티티로 반환
	 */
	@Transactional
	public ChatRoom getOrCreateChatRoomForQuote(Long proposalId, ChatRoom.OriginType originType, Long buyerId, Long sellerId) {
		return getOrCreateChatRoomEntity(proposalId, originType, buyerId, sellerId);
	}

	/**
	 * 공통 채팅방 생성/조회 핵심 도메인 로직 (P0-2, P1-6 구현)
	 */
	private ChatRoom getOrCreateChatRoomEntity(Long proposalId, ChatRoom.OriginType requestedOriginType, Long buyerId, Long sellerId) {
		// [P0-2] originType 검증 및 자동 세팅 규칙
		ChatRoom.OriginType resolvedOriginType;
		if (proposalId != null) {
			resolvedOriginType = ChatRoom.OriginType.PROPOSAL;
		} else {
			if (requestedOriginType == ChatRoom.OriginType.PROPOSAL) {
				throw new IllegalArgumentException("proposalId 없이 PROPOSAL 타입의 채팅방을 생성할 수 없습니다.");
			}
			resolvedOriginType = ChatRoom.OriginType.INQUIRY;
		}

		// 1. Proposal 기반 진입인 경우 중복 방지 (기존 방 재사용)
		if (resolvedOriginType == ChatRoom.OriginType.PROPOSAL && proposalId != null) {
			ChatRoom existingRoom = chatRoomRepository.findByProposalId(proposalId).orElse(null);
			if (existingRoom != null) {
				return existingRoom;
			}
		}

		// 2. [P1-6] INQUIRY 기반 진입인 경우 기존 ACTIVE 방 중복 방지 (재사용)
		if (resolvedOriginType == ChatRoom.OriginType.INQUIRY) {
			ChatRoom existingInquiryRoom = chatRoomRepository.findByBuyerIdAndSellerIdAndOriginTypeAndStatus(
					buyerId, sellerId, ChatRoom.OriginType.INQUIRY, ChatRoom.Status.ACTIVE
			).orElse(null);

			if (existingInquiryRoom != null) {
				return existingInquiryRoom;
			}
		}

		// 3. 신규 채팅방 생성
		ChatRoom chatRoom = ChatRoom.builder()
				.proposalId(proposalId)
				.quoteId(null)
				.originType(resolvedOriginType)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.build();

		return chatRoomRepository.save(chatRoom);
	}

	/**
	 * [P0-1] 타 서비스 전용: 채팅방 상태 닫기 (Close)
	 */
	@Transactional
	public void closeChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("연결된 채팅방을 찾을 수 없습니다. ID: " + chatRoomId));
		chatRoom.close();
	}

	/**
	 * [P0-1] 타 서비스 전용: 채팅방 최신 Quote ID 업데이트
	 */
	@Transactional
	public ChatRoom updateChatRoomQuoteId(Long chatRoomId, Long quoteId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));
		chatRoom.updateQuoteId(quoteId);
		return chatRoom;
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