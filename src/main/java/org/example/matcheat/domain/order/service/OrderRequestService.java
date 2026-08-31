package org.example.matcheat.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.common.location.GeocodingService;
import org.example.matcheat.domain.order.dto.OrderRequestCreateDTO;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.dto.OrderRequestUpdateDTO;
import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.order.enums.RequestStatus;
import org.example.matcheat.domain.order.repository.OrderRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 주문 요청의 등록, 조회, 수정, 취소, 검색 관련 비즈니스 로직을 담당하는 Service
 */
@Service // 비즈니스 로직 담당 부여
@RequiredArgsConstructor // final 필드인 Repository를 생성자로 주입
public class OrderRequestService {
    private final OrderRequestRepository orderRequestRepository;
    private final GeocodingService geocodingService;

    /**
     * {@code @Transactional}: 이 메서드의 DB 작업을 하나의 트랜잭션으로 처리
     * = 이 메서드 안의 DB 작업은 모두 성공하거나, 문제가 생기면 모두 되돌린다.
     */
    /**
     * 주문 요청을 등록한다.
     */
    @Transactional
    public OrderRequestResponseDTO create(
            Long buyerId,
            OrderRequestCreateDTO dto
    ) {
        GeocodingService.Coordinates coordinates =
                geocodingService.geocode(
                        dto.getDeliveryAddress()
                );

        OrderRequest orderRequest =
                OrderRequest.create(
                        buyerId,
                        dto.getTitle(),
                        dto.getDescription(),
                        dto.getEventDateTime(),
                        dto.getQuantity(),
                        dto.getBudgetType(),
                        dto.getBudget(),
                        dto.getCategory(),
                        dto.getDeliveryAddress(),
                        coordinates.latitude(),
                        coordinates.longitude()
                );

        OrderRequest savedOrderRequest =
                orderRequestRepository.save(
                        orderRequest
                );

        return OrderRequestResponseDTO.from(
                savedOrderRequest
        );
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

    /**
     * 현재 구매자가 등록한 주문 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<OrderRequestResponseDTO> findByBuyerId(Long buyerId) {
        return orderRequestRepository
                .findAllByBuyerIdOrderByIdDesc(buyerId)
                .stream()
                .map(OrderRequestResponseDTO::from)
                .toList();
    }

    /**
     * 전체 주문 요청 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<OrderRequestResponseDTO> findAll() {
        return orderRequestRepository.findAll()
                .stream()
                .map(OrderRequestResponseDTO::from)
                .toList();
    }

    /**
     * MATCHING 상태의 주문 요청 정보를 수정
     */
    @Transactional
    public OrderRequestResponseDTO update(Long id, Long buyerId, OrderRequestUpdateDTO dto) {
        OrderRequest orderRequest = orderRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 주문 요청입니다. id=%s".formatted(id)
                ));

        if (!orderRequest.getBuyerId().equals(buyerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "본인이 등록한 주문만 수정할 수 있습니다."
            );
        }

        if (orderRequest.getStatus() != RequestStatus.MATCHING) {
            throw new IllegalStateException(
                    "MATCHING 상태의 주문 요청만 수정할 수 있습니다."
            );
        }

        Double latitude = null;
        Double longitude = null;

        if (dto.getDeliveryAddress() != null
                && !dto.getDeliveryAddress().isBlank()) {

            GeocodingService.Coordinates coordinates =
                    geocodingService.geocode(
                            dto.getDeliveryAddress()
                    );

            latitude = coordinates.latitude();
            longitude = coordinates.longitude();
        }

        orderRequest.update(
                dto.getTitle(),
                dto.getDescription(),
                dto.getEventDateTime(),
                dto.getQuantity(),
                dto.getBudgetType(),
                dto.getBudget(),
                dto.getCategory(),
                dto.getDeliveryAddress(),
                latitude,
                longitude
        );

        return OrderRequestResponseDTO.from(orderRequest);
    }

    /**
     * MATCHING 상태의 주문 요청을 취소
     */
    @Transactional
    public OrderRequestResponseDTO cancel(Long id, Long buyerId) {
        OrderRequest orderRequest = orderRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 주문 요청입니다. id=%s".formatted(id)
                ));

        if (!orderRequest.getBuyerId().equals(buyerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "본인이 등록한 주문만 취소할 수 있습니다."
            );
        }

        orderRequest.cancel();

        return OrderRequestResponseDTO.from(orderRequest);
    }

    /**
     * 제목 또는 음식 카테고리 키워드로 주문 요청을 검색
     */
    @Transactional(readOnly = true)
    public List<OrderRequestResponseDTO> searchByKeyword(String keyword) {
        return orderRequestRepository.searchByKeyword(keyword)
                .stream()
                .map(OrderRequestResponseDTO::from)
                .toList();
    }
}
