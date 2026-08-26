package org.example.matcheat.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {
	private Long chatRoomId;
	private Long proposalId;
	private Long quoteId;
	private String originType;
	private Long buyerId;
	private Long sellerId;
	private LocalDateTime createdAt;

	public static ChatRoomResponse from(ChatRoom chatRoom) {
		return ChatRoomResponse.builder()
				.chatRoomId(chatRoom.getId())
				.proposalId(chatRoom.getProposalId())
				.quoteId(chatRoom.getQuoteId())
				.originType(chatRoom.getOriginType() != null ? chatRoom.getOriginType().name() : null)
				.buyerId(chatRoom.getBuyerId())
				.sellerId(chatRoom.getSellerId())
				.createdAt(chatRoom.getCreatedAt())
				.build();
	}
}