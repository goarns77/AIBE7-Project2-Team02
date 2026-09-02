// 배치 경로 예시: src/main/java/org/example/matcheat/domain/chat/controller/ChatPageController.java
// (다른 도메인의 페이지 컨트롤러 패턴과 이름/위치를 맞춰서 옮겨도 무방합니다.)
//
// SecurityConfig의 authorizeHttpRequests 마지막 줄이 anyRequest().permitAll() 이므로
// 별도 permitAll 매처를 추가하지 않아도 /chat 경로는 이미 열려 있습니다.
// (다만 로그인 여부는 JS의 토큰 체크(getToken 없으면 /login 리다이렉트)로 처리됩니다.)

package org.example.matcheat.domain.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatPageController {

	// /chat            -> 채팅방 목록만 보여주고 첫 채팅방 자동 선택
	// /chat?roomId=12  -> 특정 채팅방을 바로 열어서 보여줌 (다른 화면에서 딥링크로 사용)
	@GetMapping("/chat")
	public String chatRoomPage(@RequestParam(required = false) Long roomId) {
		return "chat/room";
	}
}