package org.example.matcheat.domain.estimate.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.common.location.GeocodingService;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.estimate.dto.EstimateCreateDTO;
import org.example.matcheat.domain.estimate.dto.EstimateResponseDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 로그인 사용자(accountId)를 기준으로 Estimate에 대한 권한과 자격을 검증하는 서비스이다.
 * <p>
 * 실제 Estimate 저장과 조회는 {@link EstimateService}가 담당하고,
 * 이 서비스는 요청자가 구매자/판매자 본인인지, sellerId가 승인된 판매자인지,
 * productId가 그 판매자 소유가 맞는지를 검증하고 주소를 지오코딩한 뒤 EstimateService에 위임한다.
 * ProposalAccessService와 같은 위치이다.
 * <p>
 * request_id는 더 이상 OrderRequest를 가리키는 FK가 아니라, 요청자(구매자) 본인의 계정 ID를
 * 그대로 저장한다 — 구매자가 사전에 주문 요청을 등록하지 않았어도 견적을 요청할 수 있도록 하기 위함이다.
 * 그래서 견적에 필요한 값(예산, 행사일자, 항목명, 주소 등)은 전부 이 화면에서 직접 입력받는다.
 */
public class EstimateAccessService {

    private final EstimateService estimateService;
    private final SellerApplicationRepository sellerApplicationRepository;
    private final ProductService productService;
    private final GeocodingService geocodingService;

    /**
     * 구매자가 특정 판매자(sellerId)에게 견적을 요청한다. 로그인한 본인의 accountId가
     * request_id로 그대로 저장된다. sellerId의 승인 여부, productId의 소유권을 검증하고
     * 배송(행사) 주소를 지오코딩한 뒤 저장한다.
     */
    @Transactional
    public EstimateResponseDTO create(EstimateCreateDTO dto, Long requesterAccountId) {
        if (requesterAccountId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        requireApprovedSeller(dto.getSellerId());
        verifyProductOwnedBySeller(dto.getProductId(), dto.getSellerId());

        GeocodingService.Coordinates coordinates = geocodingService.geocode(dto.getDeliveryAddress());

        return estimateService.create(
                requesterAccountId,
                dto.getSellerId(),
                dto.getProductId(),
                dto.getDescription(),
                dto.getBudget(),
                dto.getBudgetType(),
                dto.getItemName(),
                dto.getQuantity(),
                dto.getEventDateTime(),
                dto.getEstimateImage(),
                dto.getDeliveryAddress(),
                coordinates.latitude(),
                coordinates.longitude()
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
     * 내가 구매자로서 보낸 견적 요청 목록을 조회한다. request_id가 곧 내 계정 ID이므로
     * 그 값으로 바로 조회한다.
     */
    public List<EstimateResponseDTO> findSentByMe(Long requesterAccountId) {
        if (requesterAccountId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return estimateService.findByRequestId(requesterAccountId);
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
     * 요청자가 이 견적의 구매자(request_id) 또는 판매자(sellerId) 본인인지 검증한다.
     */
    private void verifyAccess(EstimateResponseDTO estimate, Long requesterAccountId) {
        if (requesterAccountId == null
                || (!requesterAccountId.equals(estimate.getSellerId())
                    && !requesterAccountId.equals(estimate.getRequestId()))) {
            throw new IllegalArgumentException("본인과 관련된 견적만 조회할 수 있습니다.");
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
}
