package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.entity.ChatMessage;
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
	private final ChatRoomRepository chatRoomRepository; // [P2-8 추가] 채팅방 존재 검증용

	/**
	 * 웹소켓 메시지 저장
	 */
	@Transactional
	public ChatMessageResponse saveMessage(ChatMessageCreateRequest request) {
		// [P2-8] 1. chatRoomId 존재 여부 사전 검증
		chatRoomRepository.findById(request.getChatRoomId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. ID: " + request.getChatRoomId()));

		// TODO: [P2-8] Security Principal 연동 시 request.getSenderId() 대신 SecurityContext의 인증된 userId를 사용하도록 교체 필요

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
				.messageType(request.getMessageType()) // Enum 그대로 주입
				.chatFile(chatFile) // FK 연결
				.build();

		ChatMessage savedMessage = chatMessageRepository.save(message);

		return ChatMessageResponse.from(savedMessage);
	}

	/**
	 * 채팅방 이전 대화 내역 조회 (Fetch Join으로 파일 정보 포함)
	 */
	public List<ChatMessageResponse> getChatHistory(Long chatRoomId) {
		// TODO: [P2-8] Security Principal 연동 시 요청자가 해당 chatRoomId의 참여자(buyer/seller)인지 확인하는 권한 검증 추가 필요
		List<ChatMessage> messages = chatMessageRepository.findHistoryWithFilesByChatRoomId(chatRoomId);
		return messages.stream()
				.map(ChatMessageResponse::from)
				.collect(Collectors.toList());
	}
}