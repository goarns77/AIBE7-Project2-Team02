package org.example.matcheat.domain.quote.repository;

import org.example.matcheat.domain.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
	List<Quote> findByChatRoomIdOrderByIdDesc(Long chatRoomId);
}