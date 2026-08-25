package org.example.matcheat.orderrequest.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.orderrequest.dto.OrderRequestCreateDTO;
import org.example.matcheat.orderrequest.dto.OrderRequestResponseDTO;
import org.example.matcheat.orderrequest.entity.OrderRequest;
import org.example.matcheat.orderrequest.repository.OrderRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 요청 등록과 상세 조회의 비즈니스 로직을 담당하는 Service
 */
@Service // 비즈니스 로직 담당 부여
@RequiredArgsConstructor // final 필드인 Repository를 생성자로 주입
public class OrderRequestService {
    private final OrderRequestRepository orderRequestRepository;

    /**
     * {@code @Transactional}: 이 메서드의 DB 작업을 하나의 트랜잭션으로 처리
     * = 이 메서드 안의 DB 작업은 모두 성공하거나, 문제가 생기면 모두 되돌린다.
     */
    @Transactional
    public OrderRequestResponseDTO create(OrderRequestCreateDTO dto) {
        OrderRequest orderRequest = OrderRequest.create(
                dto.getEventDateTime(),
                dto.getQuantity(),
                dto.getBudgetType(),
                dto.getBudget(),
                dto.getCategory(),
                dto.getDeliveryAddress(),
                dto.getLatitude(),
                dto.getLongitude()
        );

        OrderRequest savedOrderRequest = orderRequestRepository.save(orderRequest); // 실제 DB 저장

        return OrderRequestResponseDTO.from(savedOrderRequest); // 저장된 Entity를 응답용 DTO로 변환
    }

    /**
     * 주문 요청 ID로 상세 정보를 조회
     * 이 트랜잭션에서는 DB 데이터를 변경하지 않고 조회만 한다.
     */
    @Transactional(readOnly = true)
    public OrderRequestResponseDTO findById(Long id) {
        OrderRequest orderRequest = orderRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 주문 요청입니다. id=%s".formatted(id)
                ));
        return OrderRequestResponseDTO.from(orderRequest);
    }
}
