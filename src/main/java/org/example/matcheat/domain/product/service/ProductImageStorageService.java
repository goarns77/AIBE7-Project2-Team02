package org.example.matcheat.domain.product.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
/**
 * 판매 조건 이미지 파일을 저장 가능한 형태(Base64 데이터 URL)로 변환하는 서비스이다.
 */
public class ProductImageStorageService {

    /**
     * 업로드된 이미지 파일을 Base64 데이터 URL 형태로 변환해 저장 가능한 문자열로 만든다.
     */
    public String storeImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        byte[] bytes = imageFile.getBytes();
        String encoded = Base64.getEncoder().encodeToString(bytes);
        String normalizedContentType = contentType.toLowerCase();
        return "data:" + normalizedContentType + ";base64," + encoded;
    }
}
