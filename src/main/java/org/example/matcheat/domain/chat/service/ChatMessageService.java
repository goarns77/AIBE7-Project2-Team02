package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatFileRepository;
import org.example.matcheat.domain.chat.repository.ChatMessageRepository;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatFileRepository chatFileRepository;
	private final ChatRoomRepository chatRoomRepository;

	/**
	 * 웹소켓 메시지 저장.
	 * [수정] senderId를 request에서 받지 않고 파라미터로 받은 currentUserId를 그대로 쓴다.
	 * [수정] 채팅방 참여자인지 검증 추가 (기존엔 존재 여부만 확인하고 참여자 검증은 없었음).
	 */
	@Transactional
	public ChatMessageResponse saveMessage(ChatMessageCreateRequest request, Long currentUserId) {
		ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. ID: " + request.getChatRoomId()));

		chatRoom.validateParticipant(currentUserId);

		ChatFile chatFile = null;
		if (request.getFileId() != null) {
			chatFile = chatFileRepository.findById(request.getFileId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다. ID: " + request.getFileId()));
		}

		ChatMessage message = ChatMessage.builder()
				.chatRoomId(request.getChatRoomId())
				.senderId(currentUserId) // request.getSenderId() 대신 서버가 결정한 값 사용
				.content(request.getMessage())
				.messageType(request.getMessageType())
				.chatFile(chatFile)
				.build();

		ChatMessage savedMessage = chatMessageRepository.save(message);

		return ChatMessageResponse.from(savedMessage);
	}

	/**
	 * 채팅방 이전 대화 내역 조회.
	 * [수정] 참여자 검증 추가 (기존엔 누구나 chatRoomId만 알면 조회 가능했음).
	 */
	public List<ChatMessageResponse> getChatHistory(Long chatRoomId, Long currentUserId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. ID: " + chatRoomId));

		chatRoom.validateParticipant(currentUserId);

		List<ChatMessage> messages = chatMessageRepository.findHistoryWithFilesByChatRoomId(chatRoomId);
		return messages.stream()
				.map(ChatMessageResponse::from)
				.collect(Collectors.toList());
	}
}