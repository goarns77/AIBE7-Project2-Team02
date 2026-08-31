package org.example.matcheat.domain.proposal.repository;

import org.example.matcheat.domain.proposal.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 수주 제안 데이터의 저장과 조회를 담당하는 Repository이다.
 */
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByRequestIdOrderByCreatedAtDesc(Long requestId);

    List<Proposal> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}