package org.example.matcheat.domain.estimate.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.estimate.dto.EstimateCreateDTO;
import org.example.matcheat.domain.estimate.dto.EstimateResponseDTO;
import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.order.enums.BudgetType;
import org.example.matcheat.domain.order.repository.OrderRequestRepository;
import org.example.matcheat.domain.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 로그인 사용자(accountId)를 기준으로 Estimate에 대한 권한과 자격을 검증하는 서비스이다.
 * <p>
 * 실제 Estimate 저장과 조회는 {@link EstimateService}가 담당하고,
 * 이 서비스는 요청자가 구매자/판매자 본인인지, sellerId가 승인된 판매자인지,
 * productId가 그 판매자 소유가 맞는지를 검증한 뒤 EstimateService에 위임한다.
 * ProposalAccessService와 같은 위치이다.
 */
public class EstimateAccessService {

    private final EstimateService estimateService;
    private final OrderRequestRepository orderRequestRepository;
    private final SellerApplicationRepository sellerApplicationRepository;
    private final ProductService productService;

    /**
     * 구매자가 자신의 주문 요청(requestId)을 근거로 특정 판매자에게 견적을 요청한다.
     * requestId 소유자, sellerId의 승인 여부, productId의 소유권을 검증한 뒤
     * DTO에 없는 값은 주문 요청 값으로 채워 저장한다.
     */
    @Transactional
    public EstimateResponseDTO create(Long requestId, EstimateCreateDTO dto, Long requesterAccountId) {
        OrderRequest orderRequest = loadOrderRequest(requestId);
        verifyBuyer(orderRequest, requesterAccountId);
        requireApprovedSeller(dto.getSellerId());
        verifyProductOwnedBySeller(dto.getProductId(), dto.getSellerId());

        return estimateService.create(
                requestId,
                dto.getSellerId(),
                dto.getProductId(),
                resolveDescription(dto, orderRequest),
                resolveBudget(dto, orderRequest),
                resolveBudgetType(dto, orderRequest),
                resolveItemName(dto, orderRequest),
                resolveEventDateTime(dto, orderRequest),
                dto.getEstimateImage()
        );
    }

    /**
     * 견적 상세를 조회한다. 요청자가 이 견적의 구매자 또는 판매자인 경우에만 허용한다.
     */
    public EstimateResponseDTO findById(Long id, Long requesterAccountId) {
        EstimateResponseDTO estimate = estimateService.findById(id);
        verifyAccess(estimate, requesterAccountId);

        return estimate;
    }

    /**
     * 전체 견적 목록을 조회한다. 관리자만 허용한다.
     */
    public List<EstimateResponseDTO> findAll(boolean requesterIsAdmin) {
        if (!requesterIsAdmin) {
            throw new IllegalArgumentException("전체 견적 목록은 관리자만 조회할 수 있습니다.");
        }

        return estimateService.findAll();
    }

    /**
     * 구매자 본인의 주문 요청(requestId)에 달린 견적 목록을 조회한다.
     */
    public List<EstimateResponseDTO> findByRequestId(Long requestId, Long requesterAccountId) {
        OrderRequest orderRequest = loadOrderRequest(requestId);
        verifyBuyer(orderRequest, requesterAccountId);

        return estimateService.findByRequestId(requestId);
    }

    /**
     * 내가 구매자로서 보낸 견적 요청 목록을 조회한다.
     * 내 주문 요청들의 ID를 먼저 구한 뒤, 그 ID들에 달린 견적을 조회한다.
     */
    public List<EstimateResponseDTO> findSentByMe(Long requesterAccountId) {
        if (requesterAccountId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        List<Long> myRequestIds = orderRequestRepository.findAll().stream()
                .filter(orderRequest -> requesterAccountId.equals(orderRequest.getBuyerId()))
                .map(OrderRequest::getId)
                .toList();

        return estimateService.findByRequestIdIn(myRequestIds);
    }

    /**
     * 내가 판매자로서 받은 견적 요청 목록을 조회한다.
     */
    public List<EstimateResponseDTO> findReceivedByMe(Long requesterAccountId) {
        if (requesterAccountId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return estimateService.findBySellerId(requesterAccountId);
    }

    /**
     * requestId로 주문 요청을 조회한다. 존재하지 않으면 예외를 던진다.
     */
    private OrderRequest loadOrderRequest(Long requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId는 필수입니다.");
        }

        return orderRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 요청입니다. id=" + requestId));
    }

    /**
     * 요청자가 이 주문 요청을 등록한 구매자 본인인지 검증한다.
     */
    private void verifyBuyer(OrderRequest orderRequest, Long requesterAccountId) {
        if (requesterAccountId == null || !orderRequest.getBuyerId().equals(requesterAccountId)) {
            throw new IllegalArgumentException("본인이 등록한 주문 요청에 대해서만 처리할 수 있습니다.");
        }
    }

    /**
     * sellerId가 승인된(APPROVED) 판매자인지 검증한다.
     */
    private void requireApprovedSeller(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 필수입니다.");
        }

        SellerVerificationStatus status = sellerApplicationRepository.findStatusByUserId(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다. sellerId=" + sellerId));

        if (status != SellerVerificationStatus.APPROVED) {
            throw new IllegalArgumentException("승인된 판매자에게만 견적을 요청할 수 있습니다.");
        }
    }

    /**
     * productId가 주어진 경우, 그 상품이 실제로 sellerId 소유인지 검증한다.
     * ProductService.findOwnedById()를 재사용해 상품 존재 여부와 소유권을 함께 확인한다.
     */
    private void verifyProductOwnedBySeller(Long productId, Long sellerId) {
        if (productId == null) {
            return;
        }

        productService.findOwnedById(productId, sellerId);
    }

    /**
     * 요청자가 이 견적의 구매자(주문 요청 buyerId) 또는 판매자(sellerId)인지 검증한다.
     */
    private void verifyAccess(EstimateResponseDTO estimate, Long requesterAccountId) {
        if (requesterAccountId != null && requesterAccountId.equals(estimate.getSellerId())) {
            return;
        }

        OrderRequest orderRequest = loadOrderRequest(estimate.getRequestId());
        verifyBuyer(orderRequest, requesterAccountId);
    }

    private String resolveDescription(EstimateCreateDTO dto, OrderRequest orderRequest) {
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            return dto.getDescription();
        }

        return orderRequest.getDescription();
    }

    private BigDecimal resolveBudget(EstimateCreateDTO dto, OrderRequest orderRequest) {
        return dto.getBudget() != null ? dto.getBudget() : orderRequest.getBudget();
    }

    private BudgetType resolveBudgetType(EstimateCreateDTO dto, OrderRequest orderRequest) {
        return dto.getBudgetType() != null ? dto.getBudgetType() : orderRequest.getBudgetType();
    }

    private String resolveItemName(EstimateCreateDTO dto, OrderRequest orderRequest) {
        if (dto.getItemName() != null && !dto.getItemName().isBlank()) {
            return dto.getItemName();
        }

        return orderRequest.getTitle();
    }

    private LocalDateTime resolveEventDateTime(EstimateCreateDTO dto, OrderRequest orderRequest) {
        return dto.getEventDateTime() != null ? dto.getEventDateTime() : orderRequest.getEventDateTime();
    }
}
