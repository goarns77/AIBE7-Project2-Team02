package org.example.matcheat.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.product.dto.ProductCreateDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.dto.ProductUpdateDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        // TODO: 로그인 담당 개발이 끝나면 현재 로그인한 사용자의 accountId를 전달해서
        //       본인 상품으로 저장되도록 바꾼다.
        ProductResponseDTO response = productService.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 새로운 판매 조건을 등록한다. (multipart)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> createMultipart(
            @Valid @RequestPart("product") ProductCreateDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        // TODO: 로그인 담당 개발이 끝나면 현재 로그인한 사용자의 accountId를 전달해서
        //       본인 상품으로 저장되도록 바꾼다.
        ProductResponseDTO response = productService.create(dto, imageFile);

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
     * 수량, 카테고리, 1인분 가격 조건으로 판매 조건을 조회한다.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> search(
            @RequestParam(required = false) String quantity,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String servingPrice
    ) {
        List<ProductResponseDTO> response = productService.search(quantity, category, servingPrice);

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

    /**
     * 판매 조건 ID에 해당하는 항목을 부분 수정한다. (multipart)
     */
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> updateMultipart(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductUpdateDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        ProductResponseDTO response = productService.update(id, dto, imageFile);

        return ResponseEntity.ok(response);
    }

    /**
     * 판매 조건을 소프트 삭제한다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> softDelete(
            @PathVariable Long id
    ) {
        // TODO: 로그인 담당 개발이 끝나면 현재 로그인한 사용자의 accountId를 주입받아
        //       본인 상품만 삭제하도록 바꾼다.
        ProductResponseDTO response = productService.softDelete(id);

        return ResponseEntity.ok(response);
    }
}
