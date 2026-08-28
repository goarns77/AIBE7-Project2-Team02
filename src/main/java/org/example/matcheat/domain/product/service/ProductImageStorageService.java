package org.example.matcheat.domain.product.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class ProductImageStorageService {

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
