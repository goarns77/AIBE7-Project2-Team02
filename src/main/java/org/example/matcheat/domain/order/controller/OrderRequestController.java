package org.example.matcheat.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.order.dto.OrderRequestCreateDTO;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.dto.OrderRequestUpdateDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 요청 관련 HTTP 요청을 받아 Service와 연결하는 Controller
 * <p>
 * {@code @Valid}: DTO에 작성한 Validation 조건을 검사
 * {@code @RequestBody}: 들어온 JSON을 우리가 만든 DTO로 변환
 */
@RestController // HTTP 요청을 처리하고 결과를 JSON으로 반환하는 Controller
@RequestMapping("/api/v1/requests") // Controller가 담당하는 기본 주소
@RequiredArgsConstructor
public class OrderRequestController {
    private final OrderRequestService orderRequestService;

    /**
     * 새로운 주문 요청을 등록
     */
    @PostMapping
    public ResponseEntity<OrderRequestResponseDTO> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderRequestCreateDTO dto
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        OrderRequestResponseDTO response =
                orderRequestService.create(userId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 주문 요청 ID로 상세 정보를 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderRequestResponseDTO> findById(
            @PathVariable Long id
    ) {
        OrderRequestResponseDTO response = orderRequestService.findById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * 전체 주문 요청 목록을 조회
     */
    @GetMapping
    public ResponseEntity<List<OrderRequestResponseDTO>> findAll() {
        List<OrderRequestResponseDTO> response = orderRequestService.findAll();

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 구매자가 등록한 주문 목록을 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<OrderRequestResponseDTO>> findMine(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(
                orderRequestService.findByBuyerId(userId)
        );
    }

    /**
     * 주문 요청 ID로 기존 주문 요청 정보를 수정
     */
    @PatchMapping("/{id}")
    public ResponseEntity<OrderRequestResponseDTO> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestUpdateDTO dto
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        OrderRequestResponseDTO response =
                orderRequestService.update(id, userId, dto);

        return ResponseEntity.ok(response);
    }

    /**
     * MATCHING 상태의 주문 요청을 취소
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderRequestResponseDTO> cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        OrderRequestResponseDTO response =
                orderRequestService.cancel(id, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 제목 또는 음식 카테고리 키워드로 주문 요청을 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<OrderRequestResponseDTO>> searchByKeyword(
            @RequestParam String keyword
    ) {
        List<OrderRequestResponseDTO> response =
                orderRequestService.searchByKeyword(keyword);

        return ResponseEntity.ok(response);
    }
}
