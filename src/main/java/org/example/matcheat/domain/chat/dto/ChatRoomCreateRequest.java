package org.example.matcheat.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatRoom.OriginType;

@Getter
@NoArgsConstructor
public class ChatRoomCreateRequest {
	private Long sellerId;
	private Long proposalId;       // 선택 (OriginType이 PROPOSAL일 때만)
	private OriginType originType; // PROPOSAL 또는 INQUIRY
}