package org.example.matcheat.domain.proposal.service;

import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.proposal.dto.ProposalCreateDTO;
import org.example.matcheat.domain.proposal.dto.ProposalResponseDTO;
import org.example.matcheat.domain.proposal.entity.Proposal;
import org.example.matcheat.domain.proposal.enums.ProposalStatus;
import org.example.matcheat.domain.proposal.repository.ProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProposalService의 수주 제안 등록 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private OrderRequestService orderRequestService;

    @InjectMocks
    private ProposalService proposalService;

    @Test
    void 수주_제안을_정상적으로_등록한다() {
        Long requestId = 1L;
        Long sellerId = 10L;

        ProposalCreateDTO dto = mock(ProposalCreateDTO.class);
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);

        when(dto.getProductId()).thenReturn(3L);
        when(dto.getItemName()).thenReturn("프리미엄 핑거푸드 A세트");
        when(dto.getQuantity()).thenReturn(100);
        when(dto.getUnitPrice()).thenReturn(18000L);
        when(dto.getTotalAmount()).thenReturn(1700000L);
        when(dto.getPreparationDays()).thenReturn(3);
        when(dto.getDescription()).thenReturn("음료 포함 구성으로 준비 가능합니다.");

        when(orderRequestService.findById(requestId)).thenReturn(order);
        when(proposalRepository.save(any(Proposal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponseDTO result =
                proposalService.create(requestId, sellerId, dto);

        ArgumentCaptor<Proposal> captor =
                ArgumentCaptor.forClass(Proposal.class);

        verify(proposalRepository).save(captor.capture());

        Proposal savedProposal = captor.getValue();

        assertThat(savedProposal.getRequestId()).isEqualTo(requestId);
        assertThat(savedProposal.getSellerId()).isEqualTo(sellerId);
        assertThat(savedProposal.getProductId()).isEqualTo(3L);
        assertThat(savedProposal.getItemName()).isEqualTo("프리미엄 핑거푸드 A세트");
        assertThat(savedProposal.getQuantity()).isEqualTo(100);
        assertThat(savedProposal.getUnitPrice()).isEqualTo(18000L);
        assertThat(savedProposal.getTotalAmount()).isEqualTo(1700000L);
        assertThat(savedProposal.getPreparationDays()).isEqualTo(3);
        assertThat(savedProposal.getStatus()).isEqualTo(ProposalStatus.SENT);

        assertThat(result.getItemName())
                .isEqualTo("프리미엄 핑거푸드 A세트");
    }

    @Test
    void 존재하지_않는_주문에는_수주_제안을_등록할_수_없다() {
        Long requestId = 999L;
        Long sellerId = 10L;

        ProposalCreateDTO dto = mock(ProposalCreateDTO.class);

        when(orderRequestService.findById(requestId))
                .thenThrow(new IllegalArgumentException("주문을 찾을 수 없습니다."));

        assertThatThrownBy(
                () -> proposalService.create(requestId, sellerId, dto)
        )
                .isInstanceOf(IllegalArgumentException.class);

        verify(proposalRepository, never()).save(any());
    }

    @Test
    void 특정_주문에_들어온_수주_제안_목록을_조회한다() {
        Long requestId = 1L;

        Proposal proposal1 = Proposal.create(
                requestId,
                10L,
                3L,
                "핑거푸드 A세트",
                100,
                18000L,
                1700000L,
                3,
                "음료 포함 구성"
        );

        Proposal proposal2 = Proposal.create(
                requestId,
                20L,
                null,
                "맞춤 브런치 세트",
                100,
                16000L,
                1550000L,
                2,
                "행사 맞춤 구성"
        );

        when(proposalRepository.findByRequestIdOrderByCreatedAtDesc(requestId))
                .thenReturn(List.of(proposal1, proposal2));

        List<ProposalResponseDTO> result =
                proposalService.findByRequestId(requestId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getItemName()).isEqualTo("핑거푸드 A세트");
        assertThat(result.get(1).getItemName()).isEqualTo("맞춤 브런치 세트");
    }
}