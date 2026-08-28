package org.example.matcheat.domain.proposal.dto;

import lombok.Getter;
import org.example.matcheat.domain.proposal.entity.Proposal;
import org.example.matcheat.domain.proposal.enums.ProposalStatus;

import java.time.LocalDateTime;

/**
 * 수주 제안 조회 결과를 전달하는 응답 DTO이다.
 */
@Getter
public class ProposalResponseDTO {

    private final Long id;
    private final Long requestId;
    private final Long sellerId;
    private final Long productId;

    private final String itemName;
    private final Integer quantity;
    private final Long unitPrice;
    private final Long totalAmount;

    private final Integer preparationDays;
    private final String description;

    private final ProposalStatus status;
    private final LocalDateTime createdAt;

    private ProposalResponseDTO(Proposal proposal) {
        this.id = proposal.getId();
        this.requestId = proposal.getRequestId();
        this.sellerId = proposal.getSellerId();
        this.productId = proposal.getProductId();
        this.itemName = proposal.getItemName();
        this.quantity = proposal.getQuantity();
        this.unitPrice = proposal.getUnitPrice();
        this.totalAmount = proposal.getTotalAmount();
        this.preparationDays = proposal.getPreparationDays();
        this.description = proposal.getDescription();
        this.status = proposal.getStatus();
        this.createdAt = proposal.getCreatedAt();
    }

    /**
     * Proposal 엔티티를 응답 DTO로 변환한다.
     */
    public static ProposalResponseDTO from(Proposal proposal) {
        return new ProposalResponseDTO(proposal);
    }
}