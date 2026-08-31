package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatFileResponse;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatFileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatFileService {

	private final ChatFileRepository chatFileRepository;
	private final ChatService chatService; // [추가] 채팅방 존재/참여자 검증용

	private final String uploadDir = System.getProperty("user.dir") + "/uploads/chat-files/";

	/**
	 * [수정] senderId를 파라미터로 받되, 이 값은 클라이언트가 아니라
	 * 컨트롤러의 resolveCurrentUserId()가 결정한 값이어야 한다.
	 * [수정] 채팅방 존재 여부 + 요청자가 참여자인지 검증 추가 (기존엔 둘 다 없었음).
	 */
	@Transactional
	public ChatFileResponse uploadFile(Long chatRoomId, Long currentUserId, MultipartFile file) throws IOException {
		ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
		chatRoom.validateParticipant(currentUserId);

		if (file.isEmpty()) {
			throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
		}

		String originalFilename = file.getOriginalFilename();
		String contentType = file.getContentType();

		ChatMessage.MessageType fileType;
		if (contentType != null && contentType.startsWith("image/")) {
			fileType = ChatMessage.MessageType.IMAGE;
		} else if (contentType != null && (contentType.equals("application/pdf") || (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")))) {
			fileType = ChatMessage.MessageType.PDF;
		} else {
			throw new IllegalArgumentException("이미지(JPG, PNG 등) 및 PDF 파일만 업로드할 수 있습니다.");
		}

		File dir = new File(uploadDir);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir);
		}

		String storedFileName = UUID.randomUUID() + "_" + originalFilename;
		String filePath = uploadDir + storedFileName;

		file.transferTo(new File(filePath));

		ChatFile chatFile = ChatFile.builder()
				.chatRoomId(chatRoomId)
				.senderId(currentUserId) // 클라이언트가 보낸 값이 아니라 서버가 결정한 값
				.originalFileName(originalFilename)
				.storedFileName(storedFileName)
				.filePath(filePath)
				.fileType(fileType)
				.fileSize(file.getSize())
				.build();

		ChatFile savedFile = chatFileRepository.save(chatFile);
		return ChatFileResponse.from(savedFile);
	}

	/**
	 * [수정] 참여자 검증 추가 (기존엔 chatRoomId만 알면 누구나 목록 조회 가능했음).
	 */
	@Transactional(readOnly = true)
	public List<ChatFileResponse> getFilesByChatRoom(Long chatRoomId, Long currentUserId) {
		ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
		chatRoom.validateParticipant(currentUserId);

		return chatFileRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
				.stream()
				.map(ChatFileResponse::from)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ChatFile getChatFileEntity(Long fileId) {
		return chatFileRepository.findById(fileId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다. ID: " + fileId));
	}

	/**
	 * [신규] 다운로드 전용 조회 — 파일이 속한 채팅방의 참여자인지 검증한다.
	 * 기존 downloadFile 컨트롤러는 fileId만 알면 누구나 다운로드 가능했다.
	 */
	@Transactional(readOnly = true)
	public ChatFile getChatFileForDownload(Long fileId, Long currentUserId) {
		ChatFile chatFile = getChatFileEntity(fileId);
		ChatRoom chatRoom = chatService.getChatRoomEntity(chatFile.getChatRoomId());
		chatRoom.validateParticipant(currentUserId);
		return chatFile;
	}

	public Resource loadFileAsResource(String filePath) throws MalformedURLException {
		Path path = Paths.get(filePath);
		Resource resource = new UrlResource(path.toUri());
		if (resource.exists() || resource.isReadable()) {
			return resource;
		} else {
			throw new RuntimeException("파일을 읽을 수 없거나 존재하지 않습니다: " + filePath);
		}
	}
}