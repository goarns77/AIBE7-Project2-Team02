package org.example.matcheat.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRequestAccessService {
    private final OrderRequestService orderRequests;
    private final SellerApplicationRepository sellerApplications;

    public OrderRequestResponseDTO findAccessibleById(long requestId, long userId) {
        OrderRequestResponseDTO order = orderRequests.findById(requestId);
        if (userId == order.getBuyerId()) {
            return order;
        }
        SellerVerificationStatus sellerStatus = sellerApplications.findStatusByUserId(userId).orElse(null);
        if (sellerStatus != SellerVerificationStatus.APPROVED) {
            throw new AccessDeniedException("주문 상세는 소유자와 승인 판매자만 조회할 수 있습니다.");
        }
        return order;
    }
}
