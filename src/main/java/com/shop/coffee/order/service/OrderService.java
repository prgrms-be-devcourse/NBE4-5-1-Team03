package com.shop.coffee.order.service;

import com.shop.coffee.order.DTO.OrderSummaryDTO;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.order.dto.OrderIntegrationViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    // 전체 주문 조회 또는 주문 상태에 따른 조회 후 DTO로 변환하여 반환
    @Transactional
    public List<OrderSummaryDTO> getOrders(OrderStatus orderStatus) {
        List<Order> orders;
        if (orderStatus == null) {
            orders = orderRepository.findAll(); // 전체 주문 조회
        } else {
            orders = orderRepository.findByOrderStatus(orderStatus); // 주문 상태에 따른 조회
        }
        if (orders.isEmpty()) {
            return Collections.emptyList(); // 주문이 없을 경우 빈 리스트 반환
        }
        return orders.stream().map(OrderSummaryDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(); // createdAt 내림차순 정렬
        return orders.stream()
                .map(OrderDto::new) // Order -> OrderDto 변환
                .collect(Collectors.toList());
    }
  
    @Transactional
    public Order create(String email, String address, String zipCode, List<OrderItem> orderItems) {
        Order order = new Order(email, address, zipCode, orderItems);
        return this.orderRepository.save(order);
    }

    @Transactional
    public OrderIntegrationViewDto processPayment(String email, String address, String zipCode, List<OrderItem> orderItems) {
        Optional<Order> orderOptional = this.orderRepository.findByEmailAndOrderStatus(email, OrderStatus.SHIPPING);

        if (orderOptional.isPresent()) {
            Optional<Order> orderWithAddress = this.orderRepository.findByEmailAndOrderStatusAndAddressAndZipcode(
                    email, OrderStatus.SHIPPING, address, zipCode);

            Order newOrder = new Order(email, address, zipCode, orderItems);

            if(orderWithAddress.isPresent()) {
                return new OrderIntegrationViewDto("same_location_order_integration", orderWithAddress.get(), newOrder);
            } else {
                return new OrderIntegrationViewDto("different_location_order_integration", orderOptional.get(), newOrder);
            }
        } else {
            Order newOrder = create(email, address, zipCode, orderItems);
            return new OrderIntegrationViewDto("order_list", null, newOrder);
        }
    }
}
