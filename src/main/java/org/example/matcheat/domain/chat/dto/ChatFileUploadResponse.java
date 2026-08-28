package org.example.matcheat.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.chat.entity.ChatMessage;

@Getter
@Builder
public class ChatFileUploadResponse {
	private String fileUrl;
	private String originalFileName;
	private ChatMessage.MessageType fileType; // IMAGE 또는 PDF
	private Long fileSize;
}