package org.example.matcheat.domain.estimate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.estimate.dto.EstimateCreateDTO;
import org.example.matcheat.domain.estimate.dto.EstimateResponseDTO;
import org.example.matcheat.domain.estimate.service.EstimateAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
/**
 * 견적 관련 API 요청을 받아 서비스 계층에 전달하는 컨트롤러이다.
 * Proposal 도메인처럼 등록과 조회 API만 제공하며, 수락/거절 API는 아직 없다.
 * 권한/자격 검증은 {@link EstimateAccessService}에 위임한다.
 */
public class EstimateController {

    private final EstimateAccessService estimateAccessService;

    /**
     * 구매자가 특정 판매자에게 견적을 요청한다. request_id에는 요청자(구매자) 본인의
     * 계정 ID가 그대로 저장된다.
     */
    @PostMapping("/estimates")
    public ResponseEntity<EstimateResponseDTO> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EstimateCreateDTO dto
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(estimateAccessService.create(dto, userId));
    }

    /**
     * 견적 상세를 조회한다. 이 견적의 구매자 또는 판매자만 조회할 수 있다.
     */
    @GetMapping("/estimates/{id}")
    public ResponseEntity<EstimateResponseDTO> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(estimateAccessService.findById(id, userId));
    }

    /**
     * 전체 견적 목록을 조회한다. 관리자만 허용된다.
     */
    @GetMapping("/estimates")
    public ResponseEntity<List<EstimateResponseDTO>> findAll(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estimateAccessService.findAll(isAdmin(jwt)));
    }

    /**
     * 내가 구매자로서 보낸 견적 요청 목록을 조회한다.
     */
    @GetMapping("/estimates/sent")
    public ResponseEntity<List<EstimateResponseDTO>> findSentByMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(estimateAccessService.findSentByMe(userId));
    }

    /**
     * 내가 판매자로서 받은 견적 요청 목록을 조회한다.
     */
    @GetMapping("/estimates/received")
    public ResponseEntity<List<EstimateResponseDTO>> findReceivedByMe(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(estimateAccessService.findReceivedByMe(userId));
    }

    /**
     * JWT의 role 클레임이 ADMIN인지 확인한다.
     */
    private static boolean isAdmin(Jwt jwt) {
        return jwt != null && "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}
