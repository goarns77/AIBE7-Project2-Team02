package org.example.matcheat.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatFileResponse;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.service.ChatFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Chat File API", description = "채팅방 파일(이미지, PDF) 업로드, 조회, 다운로드 API")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatFileController {

	private final ChatFileService chatFileService;

	@Operation(summary = "파일 업로드 (이미지/PDF)", description = "채팅방에 이미지 또는 PDF 파일을 업로드합니다.")
	@PostMapping(value = "/chat-rooms/{chatRoomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ChatFileResponse> uploadFile(
			@PathVariable Long chatRoomId,
			@RequestPart("file") MultipartFile file) throws IOException {

		Long currentUserId = 1L; // 테스트용 임시 유저 ID
		ChatFileResponse response = chatFileService.uploadFile(chatRoomId, currentUserId, file);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "채팅방 파일 목록 조회", description = "특정 채팅방의 모든 파일 목록을 조회합니다.")
	@GetMapping("/chat-rooms/{chatRoomId}/files")
	public ResponseEntity<List<ChatFileResponse>> getFilesByChatRoom(@PathVariable Long chatRoomId) {
		List<ChatFileResponse> files = chatFileService.getFilesByChatRoom(chatRoomId);
		return ResponseEntity.ok(files);
	}

	@Operation(summary = "파일 다운로드", description = "fileId를 사용하여 업로드된 이미지 또는 PDF를 다운로드합니다.")
	@GetMapping("/chat-files/{fileId}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
		ChatFile chatFile = chatFileService.getChatFileEntity(fileId);
		Resource resource = chatFileService.loadFileAsResource(chatFile.getFilePath());

		// 한글 파일명 깨짐 방지 인코딩
		String encodedFileName = URLEncoder.encode(chatFile.getOriginalFileName(), StandardCharsets.UTF_8)
				.replaceAll("\\+", "%20");

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
				.body(resource);
	}
}