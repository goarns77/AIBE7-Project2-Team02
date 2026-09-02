package org.example.matcheat.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
/**
 * 주문 참고 이미지를 저장 가능한 Base64 데이터 URL로 변환한다.
 */
public class OrderRequestImageStorageService {

    /**
     * 업로드된 이미지 파일을 Base64 데이터 URL로 변환한다.
     */
    public String storeImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        String contentType = imageFile.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다."
            );
        }

        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "참고 이미지는 5MB 이하만 업로드할 수 있습니다."
            );
        }

        byte[] bytes = imageFile.getBytes();
        String encoded =
                Base64.getEncoder().encodeToString(bytes);

        return "data:"
                + contentType.toLowerCase()
                + ";base64,"
                + encoded;
    }
}