package org.example.matcheat.domain.chat.dto; // 패키지 경로 확인 필요

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
	private ChatRoom.OriginType originType;
	private ChatRoom.ChatRoomStatus status; // 👈 1. status 필드 추가
	private Long buyerId;
	private Long sellerId;
	private LocalDateTime createdAt;

	public static ChatRoomResponse from(ChatRoom chatRoom) {
		return ChatRoomResponse.builder()
				.chatRoomId(chatRoom.getId())
				.proposalId(chatRoom.getProposalId())
				.quoteId(chatRoom.getQuoteId())
				.originType(chatRoom.getOriginType())
				.status(chatRoom.getStatus()) // 👈 2. status 값 매핑 추가
				.buyerId(chatRoom.getBuyerId())
				.sellerId(chatRoom.getSellerId())
				.createdAt(chatRoom.getCreatedAt())
				.build();
	}
}