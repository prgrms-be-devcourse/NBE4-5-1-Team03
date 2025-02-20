package com.shop.coffee.order.service;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.shop.coffee.global.exception.ErrorCode.NOSINGLEORDER;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
        return new OrderDto(order);
    }

    // 전체 주문 조회 또는 주문 상태에 따른 조회
    public List<Order> getOrders(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return orderRepository.findAll(); // 전체 주문 조회
        }
        return orderRepository.findByOrderStatus(orderStatus); // 상태별 주문 조회

    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(); // createdAt 내림차순 정렬
        return orders.stream()
                .map(OrderDto::new) // Order -> OrderDto 변환
                .collect(Collectors.toList());
    }

}

