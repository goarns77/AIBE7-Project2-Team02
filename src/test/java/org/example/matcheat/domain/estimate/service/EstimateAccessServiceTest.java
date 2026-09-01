package org.example.matcheat.domain.estimate.service;

import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.estimate.dto.EstimateResponseDTO;
import org.example.matcheat.domain.order.repository.OrderRequestRepository;
import org.example.matcheat.domain.product.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstimateAccessServiceTest {
    private final EstimateService estimates = mock(EstimateService.class);
    private final OrderRequestRepository orders = mock(OrderRequestRepository.class);
    private final SellerApplicationRepository sellers = mock(SellerApplicationRepository.class);
    private final ProductService products = mock(ProductService.class);
    private final EstimateAccessService service = new EstimateAccessService(estimates, orders, sellers, products);

    @Test
    void mapsAuthenticatedSellerAccountToSellerProfileForReceivedList() {
        var seller = new SellerApplicationRepository.SellerApplication(33L, SellerVerificationStatus.APPROVED);
        List<EstimateResponseDTO> expected = List.of(mock(EstimateResponseDTO.class));
        when(sellers.findByUserId(7L)).thenReturn(Optional.of(seller));
        when(estimates.findBySellerId(33L)).thenReturn(expected);

        assertThat(service.findReceivedByMe(7L)).isSameAs(expected);
        verify(estimates).findBySellerId(33L);
    }

    @Test
    void mapsStoredSellerProfileToAccountWhenCheckingDetailAccess() {
        EstimateResponseDTO estimate = mock(EstimateResponseDTO.class);
        when(estimate.getSellerId()).thenReturn(33L);
        when(estimates.findById(5L)).thenReturn(estimate);
        when(sellers.findUserIdBySellerId(33L)).thenReturn(Optional.of(7L));

        assertThat(service.findById(5L, 7L)).isSameAs(estimate);
    }
}
