package org.example.matcheat.domain.quote.repository;

import org.example.matcheat.domain.quote.entity.QuoteNegotiation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuoteNegotiationRepository extends JpaRepository<QuoteNegotiation, Long> {
	Optional<QuoteNegotiation> findByChatRoomId(Long chatRoomId);

	boolean existsByBuyerId(Long buyerId);

	boolean existsBySellerId(Long sellerId);
}
