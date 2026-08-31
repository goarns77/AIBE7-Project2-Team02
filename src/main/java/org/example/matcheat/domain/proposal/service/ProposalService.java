package org.example.matcheat.domain.proposal.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.proposal.dto.ProposalCreateDTO;
import org.example.matcheat.domain.proposal.dto.ProposalResponseDTO;
import org.example.matcheat.domain.proposal.entity.Proposal;
import org.example.matcheat.domain.proposal.repository.ProposalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수주 제안의 등록과 조회를 처리하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final OrderRequestService orderRequestService;

    /**
     * 판매자가 특정 주문에 새로운 수주 제안을 등록한다.
     */
    @Transactional
    public ProposalResponseDTO create(
            Long requestId,
            Long sellerId,
            ProposalCreateDTO dto
    ) {
        // 존재하지 않는 주문에 제안하는 것을 방지한다.
        orderRequestService.findById(requestId);

        // 같은 판매자가 같은 주문에 최초 제안을 중복 등록하는 것을 방지한다.
        if (proposalRepository.existsByRequestIdAndSellerId(requestId, sellerId)) {
            throw new IllegalStateException(
                    "이미 해당 주문에 제안을 보냈습니다."
            );
        }

        Proposal proposal = Proposal.create(
                requestId,
                sellerId,
                dto.getProductId(),
                dto.getItemName(),
                dto.getQuantity(),
                dto.getUnitPrice(),
                dto.getTotalAmount(),
                dto.getPreparationDays(),
                dto.getDescription()
        );

        Proposal savedProposal = proposalRepository.save(proposal);

        return ProposalResponseDTO.from(savedProposal);
    }

    /**
     * 특정 주문에 들어온 수주 제안 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findByRequestId(Long requestId) {
        return proposalRepository
                .findByRequestIdOrderByCreatedAtDesc(requestId)
                .stream()
                .map(ProposalResponseDTO::from)
                .toList();
    }

    /**
     * 특정 판매자가 보낸 수주 제안 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findBySellerId(Long sellerId) {
        return proposalRepository
                .findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(ProposalResponseDTO::from)
                .toList();
    }
}