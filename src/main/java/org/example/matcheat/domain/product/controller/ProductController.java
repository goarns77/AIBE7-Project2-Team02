package org.example.matcheat.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.product.dto.ProductCreateDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.dto.ProductUpdateDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
/**
 * 판매 조건 관련 API 요청을 받아 서비스 계층에 전달하는 컨트롤러이다.
 */
public class ProductController {
    private final ProductService productService;

    /**
     * 새로운 판매 조건을 등록한다.
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(
            @Valid @RequestBody ProductCreateDTO dto
    ) {
        ProductResponseDTO response = productService.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 판매 조건 ID로 단건 조회한다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(
            @PathVariable Long id
    ) {
        ProductResponseDTO response = productService.findById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * 등록된 모든 판매 조건을 조회한다.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        List<ProductResponseDTO> response = productService.findAll();

        return ResponseEntity.ok(response);
    }

    /**
     * 판매 조건 ID에 해당하는 항목을 부분 수정한다.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update (
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO dto
    ) {
       ProductResponseDTO response = productService.update(id, dto);

       return ResponseEntity.ok(response);
    }

}
