package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatRoomCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatRoomResponse;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
import org.example.matcheat.domain.account.service.TradeAccountValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final TradeAccountValidationService accounts;

	@Transactional
	public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long currentUserId) {
		accounts.requireActiveUser(currentUserId);
		accounts.requireApprovedSeller(request.getSellerId());
		if (request.getProposalId() == null && request.getOriginType() == ChatRoom.OriginType.PROPOSAL) {
			throw new IllegalArgumentException("proposalId 없이 PROPOSAL 타입의 채팅방을 생성할 수 없습니다.");
		}

		ChatRoom chatRoom = getOrCreateChatRoomEntity(
				request.getProposalId(),
				request.getOriginType(),
				currentUserId,
				request.getSellerId()
		);
		return ChatRoomResponse.from(chatRoom);
	}

	@Transactional
	public ChatRoom getOrCreateChatRoomForQuote(Long proposalId, ChatRoom.OriginType originType, Long buyerId, Long sellerId) {
		accounts.requireActiveUser(buyerId);
		accounts.requireApprovedSeller(sellerId);
		return getOrCreateChatRoomEntity(proposalId, originType, buyerId, sellerId);
	}

	/**
	 * [추가] 외부 도메인(QuoteService 등) 전용 ChatRoom 엔티티 조회 메서드
	 */
	@Transactional(readOnly = true)
	public ChatRoom getChatRoomEntity(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. ID: " + chatRoomId));
	}

	/**
	 * [버그 수정] 기존 코드는 proposalId가 null인 경우(=Proposal 도메인이 아직 없어
	 * /quotes/direct처럼 proposalId 없이 PROPOSAL 타입 방을 만드는 모든 경로)
	 * 중복 방지 조회를 전혀 타지 않아, 같은 buyer-seller 조합으로 호출할 때마다
	 * ChatRoom이 무한히 새로 생성됐다.
	 *
	 * 수정 후 규칙:
	 * - proposalId가 있으면: 그 proposalId 기준으로만 기존 방 재사용 (오퍼 1개당 방 1개).
	 * - proposalId가 없으면: PROPOSAL/INQUIRY 상관없이 buyerId+sellerId+originType+status
	 *   기준으로 활성 상태인 기존 방을 재사용한다.
	 */
	private ChatRoom getOrCreateChatRoomEntity(Long proposalId, ChatRoom.OriginType requestedOriginType, Long buyerId, Long sellerId) {
		ChatRoom.OriginType resolvedOriginType = (proposalId != null)
				? ChatRoom.OriginType.PROPOSAL
				: (requestedOriginType != null ? requestedOriginType : ChatRoom.OriginType.INQUIRY);

		if (proposalId != null) {
			return chatRoomRepository.findByProposalId(proposalId)
					.orElseGet(() -> createNewChatRoom(proposalId, resolvedOriginType, buyerId, sellerId));
		}

		return chatRoomRepository.findByBuyerIdAndSellerIdAndOriginTypeAndStatus(
						buyerId, sellerId, resolvedOriginType, ChatRoom.Status.ACTIVE)
				.orElseGet(() -> createNewChatRoom(null, resolvedOriginType, buyerId, sellerId));
	}

	private ChatRoom createNewChatRoom(Long proposalId, ChatRoom.OriginType originType, Long buyerId, Long sellerId) {
		ChatRoom chatRoom = ChatRoom.builder()
				.proposalId(proposalId)
				.quoteId(null)
				.originType(originType)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.build();

		return chatRoomRepository.save(chatRoom);
	}

	@Transactional
	public void closeChatRoom(Long chatRoomId) {
		ChatRoom chatRoom = getChatRoomEntity(chatRoomId);
		chatRoom.close();
	}

	@Transactional
	public ChatRoom updateChatRoomQuoteId(Long chatRoomId, Long quoteId) {
		ChatRoom chatRoom = getChatRoomEntity(chatRoomId);
		chatRoom.updateQuoteId(quoteId);
		return chatRoom;
	}

	@Transactional(readOnly = true)
	public ChatRoomResponse getChatRoom(Long chatRoomId, Long currentUserId) {
		ChatRoom chatRoom = getChatRoomEntity(chatRoomId);
		chatRoom.validateParticipant(currentUserId);
		return ChatRoomResponse.from(chatRoom);
	}

	@Transactional(readOnly = true)
	public List<ChatRoomResponse> getChatRooms(Long currentUserId) {
		return chatRoomRepository.findAllByParticipant(currentUserId).stream()
				.map(ChatRoomResponse::from)
				.toList();
	}
}
