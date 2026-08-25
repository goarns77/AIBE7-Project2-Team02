package org.example.matcheat.orderrequest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.orderrequest.dto.OrderRequestCreateDTO;
import org.example.matcheat.orderrequest.dto.OrderRequestResponseDTO;
import org.example.matcheat.orderrequest.service.OrderRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody OrderRequestCreateDTO dto
    ) {
        // Service에게 주문 요청 생성을 요청
        OrderRequestResponseDTO response = orderRequestService.create(dto);

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
}
