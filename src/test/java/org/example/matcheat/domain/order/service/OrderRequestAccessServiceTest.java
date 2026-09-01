package org.example.matcheat.domain.order.service;

import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderRequestAccessServiceTest {
    private final OrderRequestService orders = mock(OrderRequestService.class);
    private final SellerApplicationRepository sellers = mock(SellerApplicationRepository.class);
    private final OrderRequestAccessService service = new OrderRequestAccessService(orders, sellers);

    @Test
    void allowsOwnerAndApprovedSeller() {
        OrderRequestResponseDTO order = orderOwnedBy(7L);
        when(orders.findById(3L)).thenReturn(order);
        when(sellers.findStatusByUserId(9L)).thenReturn(Optional.of(SellerVerificationStatus.APPROVED));

        assertThat(service.findAccessibleById(3L, 7L)).isSameAs(order);
        assertThat(service.findAccessibleById(3L, 9L)).isSameAs(order);
    }

    @Test
    void rejectsUnapprovedNonOwner() {
        OrderRequestResponseDTO order = orderOwnedBy(7L);
        when(orders.findById(3L)).thenReturn(order);
        when(sellers.findStatusByUserId(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findAccessibleById(3L, 8L))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static OrderRequestResponseDTO orderOwnedBy(Long buyerId) {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        when(order.getBuyerId()).thenReturn(buyerId);
        return order;
    }
}
