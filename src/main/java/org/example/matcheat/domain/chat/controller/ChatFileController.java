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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

	/**
	 * [수정] senderId를 @RequestParam으로 받지 않는다 — 업로더는 클라이언트가
	 * 주장하는 값이 아니라 JWT subject로 서버가 결정한다.
	 */
	@Operation(summary = "파일 업로드 (이미지/PDF)", description = "채팅방에 이미지 또는 PDF 파일을 업로드합니다. 해당 채팅방의 참여자만 업로드할 수 있습니다.")
	@PostMapping(value = "/chat-rooms/{chatRoomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ChatFileResponse> uploadFile(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long chatRoomId,
			@RequestPart("file") MultipartFile file) throws IOException {

		Long currentUserId = Long.valueOf(jwt.getSubject());
		ChatFileResponse response = chatFileService.uploadFile(chatRoomId, currentUserId, file);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "채팅방 파일 목록 조회", description = "특정 채팅방의 모든 파일 목록을 조회합니다. 참여자만 조회할 수 있습니다.")
	@GetMapping("/chat-rooms/{chatRoomId}/files")
	public ResponseEntity<List<ChatFileResponse>> getFilesByChatRoom(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long chatRoomId) {
		Long currentUserId = Long.valueOf(jwt.getSubject());
		List<ChatFileResponse> files = chatFileService.getFilesByChatRoom(chatRoomId, currentUserId);
		return ResponseEntity.ok(files);
	}

	/**
	 * [수정] fileId만 알면 누구나 다운로드 가능하던 문제를 참여자 검증으로 막는다.
	 */
	@Operation(summary = "파일 다운로드", description = "fileId를 사용하여 업로드된 이미지 또는 PDF를 다운로드합니다. 해당 채팅방 참여자만 다운로드할 수 있습니다.")
	@GetMapping("/chat-files/{fileId}/download")
	public ResponseEntity<Resource> downloadFile(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long fileId) throws IOException {
		Long currentUserId = Long.valueOf(jwt.getSubject());
		ChatFile chatFile = chatFileService.getChatFileForDownload(fileId, currentUserId);
		Resource resource = chatFileService.loadFileAsResource(chatFile.getFilePath());

		String encodedFileName = URLEncoder.encode(chatFile.getOriginalFileName(), StandardCharsets.UTF_8)
				.replaceAll("\\+", "%20");

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
				.body(resource);
	}

}
