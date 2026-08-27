package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatFileResponse;
import org.example.matcheat.domain.chat.entity.ChatFile;
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

	// 파일이 저장될 로컬 디렉토리 경로 (프로젝트 루트 하위 uploads/chat-files)
	private final String uploadDir = System.getProperty("user.dir") + "/uploads/chat-files/";

	@Transactional
	public ChatFileResponse uploadFile(Long chatRoomId, Long senderId, MultipartFile file) throws IOException { // 👈 [수정 1] uploaderId -> senderId
		if (file.isEmpty()) {
			throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
		}

		String originalFilename = file.getOriginalFilename();
		String contentType = file.getContentType();

		// 이미지 및 PDF 검증
		String fileType;
		if (contentType != null && contentType.startsWith("image/")) {
			fileType = "IMAGE";
		} else if (contentType != null && contentType.equals("application/pdf")) {
			fileType = "PDF";
		} else {
			throw new IllegalArgumentException("이미지(JPG, PNG 등) 및 PDF 파일만 업로드할 수 있습니다.");
		}

		// 저장 디렉토리 생성 및 검증
		File dir = new File(uploadDir);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("업로드 디렉토리를 생성할 수 없습니다: " + uploadDir);
		}

		// 고유 파일명 생성 (중복 방지)
		String storedFileName = UUID.randomUUID() + "_" + originalFilename;
		String filePath = uploadDir + storedFileName;

		// 로컬 파일 저장
		file.transferTo(new File(filePath));

		// DB 메타데이터 저장
		ChatFile chatFile = ChatFile.builder()
				.chatRoomId(chatRoomId)
				.senderId(senderId) // 👈 이제 senderId 변수를 정상 인식합니다.
				.originalFileName(originalFilename)
				.storedFileName(storedFileName)
				.filePath(filePath)
				.fileType(fileType)
				.fileSize(file.getSize())
				.build();

		ChatFile savedFile = chatFileRepository.save(chatFile);
		return ChatFileResponse.from(savedFile); // 👈 [수정 2] 불필요한 두 번째 인자(uploaderId) 제거
	}

	@Transactional(readOnly = true)
	public List<ChatFileResponse> getFilesByChatRoom(Long chatRoomId) {
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