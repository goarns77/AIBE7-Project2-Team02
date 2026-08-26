package org.example.matcheat.domain.quote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.quote.entity.Quote.QuoteStatus;

@Getter
@NoArgsConstructor
public class QuoteStatusUpdateRequest {
	private QuoteStatus status; // ACCEPTED, REJECTED 등
}