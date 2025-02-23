package com.shop.coffee.order.service;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.*;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.service.OrderItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.shop.coffee.global.exception.ErrorCode.NOSINGLEORDER;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
  
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
        return new OrderDto(order);
    }

    // 전체 주문 조회 또는 주문 상태에 따른 조회 후 DTO로 변환하여 반환
    @Transactional
    public List<com.shop.coffee.order.dto.OrderSummaryDto> getOrders(OrderStatus orderStatus) {
        List<Order> orders;
        if (orderStatus == null) {
            orders = orderRepository.findAll(); // 전체 주문 조회
        } else {
            orders = orderRepository.findByOrderStatus(orderStatus); // 주문 상태에 따른 조회
        }
        if (orders.isEmpty()) {
            return Collections.emptyList(); // 주문이 없을 경우 빈 리스트 반환
        }
        return orders.stream().map(com.shop.coffee.order.dto.OrderSummaryDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailDto getOrderByEmailAndModifiedAt(String email, LocalDateTime modifiedAt) {
        Optional<Order> order = orderRepository.findByEmailAndModifiedAt(email, modifiedAt);
        return order.map(AdminOrderDetailDto::new).orElseThrow(() -> new EntityNotFoundException(NOSINGLEORDER.getMessage()));
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
    public OrderIntegrationViewDto processPayment(String email, String address, String zipCode, List<ItemToOrderItemDto> items) {
        Optional<Order> orderOptional = this.orderRepository.findByEmailAndOrderStatus(email, OrderStatus.RECEIVED);

        if (orderOptional.isPresent()) {
            Optional<Order> orderWithAddress = this.orderRepository.findByEmailAndOrderStatusAndAddressAndZipcode(
                    email, OrderStatus.RECEIVED, address, zipCode);

            Order newOrder = new Order(email, address, zipCode, new ArrayList<>());
            orderItemService.createListItem(newOrder, items);

            if(orderWithAddress.isPresent()) {
                return new OrderIntegrationViewDto("same_location_order_integration", new OrderIntegrationDto(orderWithAddress.get()), new OrderIntegrationDto(newOrder));
            } else {
                return new OrderIntegrationViewDto("different_location_order_integration",  new OrderIntegrationDto(orderOptional.get()), new OrderIntegrationDto(newOrder));
            }
        } else {
            Order newOrder = new Order(email, address, zipCode, new ArrayList<>());
            orderItemService.createListItem(newOrder, items);
            orderRepository.save(newOrder);

            return new OrderIntegrationViewDto("redirect:/orders/order-list", null, new OrderIntegrationDto(newOrder));
        }
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        this.orderRepository.deleteById(orderId);
    }

    @Transactional
    public OrderDetailDto updateOrder(Long orderId, OrderDetailDto orderDetailDto) {
        Order existingOrder = orderRepository.findByIdOrderWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOSINGLEORDER.getMessage()));

        existingOrder.setAddress(orderDetailDto.getAddress());
        existingOrder.setZipcode(orderDetailDto.getZipcode());

        List<OrderItem> existingItems = existingOrder.getOrderItems();
        List<OrderItem> newItems = orderDetailDto.getOrderItems();

        Map<Long, OrderItem> existingItemMap = existingItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));

        List<OrderItem> updatedItems = new ArrayList<>();

        for (OrderItem newItem : newItems) {
            if (newItem.getId() != null && existingItemMap.containsKey(newItem.getId())) {
                OrderItem existingItem = existingItemMap.get(newItem.getId());
                existingItem.setQuantity(newItem.getQuantity());
                updatedItems.add(existingItem);
            } else {
                OrderItem orderItem = new OrderItem(existingOrder, newItem.getItem(), newItem.getPrice(), newItem.getQuantity(), newItem.getImagePath());
                updatedItems.add(orderItem);
            }
        }

        existingOrder.setOrderItems(updatedItems);

        return new OrderDetailDto(existingOrder);
    }

    //이메일로 주문 유무 확인
    public boolean emailExists(String email) {
        return orderRepository.existsByEmail(email);
    }

    @Transactional
    public OrderDetailDto getOrderDetailDtoById(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));

        return new OrderDetailDto(order);
    }

    @Transactional
    public Order getOrderByIdWithItems(long orderId) {
        return orderRepository.findByIdOrderWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
    }
}