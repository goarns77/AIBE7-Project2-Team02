package org.example.matcheat.domain.payment.repository;

import org.example.matcheat.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByQuoteId(Long quoteId);
}