package org.example.matcheat.domain.payment.repository;

import org.example.matcheat.domain.payment.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
	Optional<Settlement> findByPaymentId(Long paymentId);
}