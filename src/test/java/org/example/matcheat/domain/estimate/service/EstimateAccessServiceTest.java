package org.example.matcheat.domain.estimate.service;

import org.example.matcheat.common.location.GeocodingService;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.estimate.dto.EstimateResponseDTO;
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
    private final SellerApplicationRepository sellers = mock(SellerApplicationRepository.class);
    private final ProductService products = mock(ProductService.class);
    private final GeocodingService geocoding = mock(GeocodingService.class);
    private final EstimateAccessService service = new EstimateAccessService(estimates, sellers, products, geocoding);

    @Test
    void mapsAuthenticatedSellerAccountToSellerProfileForReceivedList() {
        var seller = new SellerApplicationRepository.SellerApplication(33L, SellerVerificationStatus.APPROVED);
        EstimateResponseDTO dto = EstimateResponseDTO.builder().id(1L).sellerId(33L).build();
        when(sellers.findByUserId(7L)).thenReturn(Optional.of(seller));
        when(estimates.findBySellerId(33L)).thenReturn(List.of(dto));

        List<EstimateResponseDTO> result = service.findReceivedByMe(7L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isSeller()).isTrue();
        assertThat(result.get(0).isBuyer()).isFalse();
        verify(estimates).findBySellerId(33L);
    }

    @Test
    void mapsStoredSellerProfileToAccountWhenCheckingDetailAccess() {
        EstimateResponseDTO estimate = EstimateResponseDTO.builder().id(5L).sellerId(33L).requestId(999L).build();
        when(estimates.findById(5L)).thenReturn(estimate);
        when(sellers.findUserIdBySellerId(33L)).thenReturn(Optional.of(7L));

        EstimateResponseDTO result = service.findById(5L, 7L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.isSeller()).isTrue();
        assertThat(result.isBuyer()).isFalse();
    }
}
