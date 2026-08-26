package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 연결된 Proposal ID
	@Column(nullable = false)
	private Long proposalId;

	// 연결된 Request ID
	@Column(nullable = false)
	private Long requestId;

	private Long buyerId;
	private Long sellerId;

	public static ChatRoom create(Long proposalId, Long requestId, Long buyerId, Long sellerId) {
		return ChatRoom.builder()
				.proposalId(proposalId)
				.requestId(requestId)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.build();
	}
}
