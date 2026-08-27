package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.example.matcheat.domain.chat.repository.ChatFileRepository;
import org.example.matcheat.domain.chat.repository.ChatMessageRepository;
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

	/**
	 * 웹소켓 메시지 저장
	 */
	@Transactional
	public ChatMessageResponse saveMessage(ChatMessageCreateRequest request) {
		ChatFile chatFile = null;

		// request에 fileId가 포함되어 전달되는 경우 (파일 메시지인 경우)
		if (request.getFileId() != null) {
			chatFile = chatFileRepository.findById(request.getFileId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다. ID: " + request.getFileId()));
		}

		ChatMessage message = ChatMessage.builder()
				.chatRoomId(request.getChatRoomId())
				.senderId(request.getSenderId())
				.content(request.getMessage())
				.messageType(request.getMessageType()) // 👈 Enum 그대로 주입
				.chatFile(chatFile) // FK 연결
				.build();

		ChatMessage savedMessage = chatMessageRepository.save(message);

		// ChatMessageResponse 내부 정적 팩토리 메서드 활용
		return ChatMessageResponse.from(savedMessage);
	}

	/**
	 * 채팅방 이전 대화 내역 조회 (Fetch Join으로 파일 정보 포함)
	 */
	public List<ChatMessageResponse> getChatHistory(Long chatRoomId) {
		List<ChatMessage> messages = chatMessageRepository.findHistoryWithFilesByChatRoomId(chatRoomId);
		return messages.stream()
				.map(ChatMessageResponse::from) // 👈 중복 메서드 대신 from() 매핑 활용
				.collect(Collectors.toList());
	}
}