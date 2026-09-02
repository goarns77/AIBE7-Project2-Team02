package org.example.matcheat.domain.quote.repository;

import org.example.matcheat.domain.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
	List<Quote> findByChatRoomIdOrderByIdDesc(Long chatRoomId);

	@Query("select (count(q) > 0) from Quote q where (q.buyerId = :userId or q.sellerId = :userId) and q.status = :status")
	boolean existsByParticipantAndStatus(@Param("userId") Long userId, @Param("status") Quote.QuoteStatus status);

	boolean existsByBuyerIdAndStatusIn(Long buyerId, Collection<Quote.QuoteStatus> statuses);

	boolean existsBySellerIdAndStatusIn(Long sellerId, Collection<Quote.QuoteStatus> statuses);
}
