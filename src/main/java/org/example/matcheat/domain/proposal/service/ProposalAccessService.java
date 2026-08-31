package org.example.matcheat.domain.proposal.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.enums.RequestStatus;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.product.service.ProductService;
import org.example.matcheat.domain.proposal.dto.ProposalCreateDTO;
import org.example.matcheat.domain.proposal.dto.ProposalResponseDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 로그인 사용자를 기준으로 Proposal 접근 권한과 판매자 자격을 검증한다.
 * <p>
 * 실제 Proposal 저장과 조회는 ProposalService가 담당하고,
 * 이 서비스는 JWT userId를 sellerId 또는 buyerId와 연결하는 역할을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalAccessService {

    private final ProposalService proposalService;
    private final OrderRequestService orderRequestService;
    private final SellerApplicationRepository sellerApplicationRepository;
    private final ProductService productService;

    /**
     * 승인된 판매자가 특정 주문에 최초 제안을 등록한다.
     */
    @Transactional
    public ProposalResponseDTO create(
            Long requestId,
            Long userId,
            ProposalCreateDTO dto
    ) {
        OrderRequestResponseDTO orderRequest =
                orderRequestService.findById(requestId);

        if (orderRequest.getBuyerId().equals(userId)) {
            throw new AccessDeniedException(
                    "본인이 등록한 주문에는 제안할 수 없습니다."
            );
        }

        if (orderRequest.getStatus() != RequestStatus.MATCHING) {
            throw new IllegalStateException(
                    "매칭 중인 주문에만 제안할 수 있습니다."
            );
        }

        SellerApplicationRepository.SellerApplication seller =
                sellerApplicationRepository.findByUserId(userId)
                        .orElseThrow(() -> new AccessDeniedException(
                                "판매자 등록이 필요한 기능입니다."
                        ));

        if (seller.status() != SellerVerificationStatus.APPROVED) {
            throw new AccessDeniedException(
                    "승인된 판매자만 제안을 보낼 수 있습니다."
            );
        }

        // 등록 상품 제안인 경우 현재 로그인 사용자의 상품인지 확인한다.
        if (dto.getProductId() != null) {
            productService.findOwnedById(
                    dto.getProductId(),
                    userId
            );
        }

        return proposalService.create(
                requestId,
                seller.sellerId(),
                dto
        );
    }

    /**
     * 현재 구매자가 자신의 주문에 받은 모든 제안을 조회한다.
     */
    public List<ProposalResponseDTO> findReceived(Long userId) {
        return orderRequestService.findByBuyerId(userId)
                .stream()
                .flatMap(orderRequest ->
                        proposalService
                                .findByRequestId(orderRequest.getId())
                                .stream()
                )
                .sorted(
                        Comparator.comparing(
                                ProposalResponseDTO::getCreatedAt
                        ).reversed()
                )
                .toList();
    }

    /**
     * 현재 판매자가 보낸 모든 제안을 조회한다.
     */
    public List<ProposalResponseDTO> findSent(Long userId) {
        return sellerApplicationRepository
                .findByUserId(userId)
                .map(seller ->
                        proposalService.findBySellerId(
                                seller.sellerId()
                        )
                )
                .orElseGet(List::of);
    }

    /**
     * 구매자가 자신이 등록한 특정 주문에 들어온 제안만 조회한다.
     */
    public List<ProposalResponseDTO> findReceivedByRequest(
            Long requestId,
            Long userId
    ) {
        OrderRequestResponseDTO orderRequest =
                orderRequestService.findById(requestId);

        if (!orderRequest.getBuyerId().equals(userId)) {
            throw new AccessDeniedException(
                    "본인이 등록한 주문의 제안만 확인할 수 있습니다."
            );
        }

        return proposalService.findByRequestId(requestId);
    }
}