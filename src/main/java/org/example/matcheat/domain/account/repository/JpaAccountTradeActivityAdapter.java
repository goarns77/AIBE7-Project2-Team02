package org.example.matcheat.domain.account.repository;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.service.AccountTradeActivityPort;
import org.example.matcheat.domain.estimate.enums.EstimateStatus;
import org.example.matcheat.domain.estimate.repository.EstimateRepository;
import org.example.matcheat.domain.order.enums.RequestStatus;
import org.example.matcheat.domain.order.repository.OrderRequestRepository;
import org.example.matcheat.domain.proposal.enums.ProposalStatus;
import org.example.matcheat.domain.proposal.repository.ProposalRepository;
import org.example.matcheat.domain.quote.entity.Quote;
import org.example.matcheat.domain.quote.repository.QuoteRepository;
import org.example.matcheat.domain.quote.repository.QuoteNegotiationRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class JpaAccountTradeActivityAdapter implements AccountTradeActivityPort {
    private static final Set<RequestStatus> ACTIVE_REQUEST_STATUSES =
            Set.of(RequestStatus.MATCHING, RequestStatus.IN_TALK, RequestStatus.CONFIRMED);
    private static final Set<ProposalStatus> ACTIVE_PROPOSAL_STATUSES =
            Set.of(ProposalStatus.SENT, ProposalStatus.IN_TALK, ProposalStatus.ACCEPTED);
    private static final Set<EstimateStatus> ACTIVE_ESTIMATE_STATUSES =
            Set.of(EstimateStatus.REQUESTED, EstimateStatus.ACCEPTED);
    private static final Set<Quote.QuoteStatus> ACTIVE_QUOTE_STATUSES =
            Set.of(Quote.QuoteStatus.SENT, Quote.QuoteStatus.ACCEPTED);

    private final OrderRequestRepository orderRequests;
    private final ProposalRepository proposals;
    private final EstimateRepository estimates;
    private final QuoteRepository quotes;
    private final QuoteNegotiationRepository negotiations;
    private final SellerApplicationRepository sellerApplications;

    @Override
    public boolean hasActiveTrade(long userId) {
        if (orderRequests.existsByBuyerIdAndStatusIn(userId, ACTIVE_REQUEST_STATUSES)
                || quotes.existsByBuyerIdAndStatusIn(userId, ACTIVE_QUOTE_STATUSES)
                || negotiations.existsByBuyerId(userId)) {
            return true;
        }
        return sellerApplications.findByUserId(userId)
                .map(SellerApplicationRepository.SellerApplication::sellerId)
                .map(sellerId -> proposals.existsBySellerIdAndStatusIn(sellerId, ACTIVE_PROPOSAL_STATUSES)
                        || estimates.existsBySellerIdAndStatusIn(sellerId, ACTIVE_ESTIMATE_STATUSES)
                        || quotes.existsBySellerIdAndStatusIn(sellerId, ACTIVE_QUOTE_STATUSES)
                        || negotiations.existsBySellerId(sellerId))
                .orElse(false);
    }
}
