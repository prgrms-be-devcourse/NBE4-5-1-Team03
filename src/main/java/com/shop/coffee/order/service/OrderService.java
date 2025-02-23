package com.shop.coffee.order.service;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.*;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.dto.OrderDetailItemDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.service.OrderItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.shop.coffee.global.exception.ErrorCode.ITEM_NOT_FOUND;
import static com.shop.coffee.global.exception.ErrorCode.NOSINGLEORDER;
import static com.shop.coffee.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    //주문 번호로 주문 단건 조회
    private final OrderItemService orderItemService;
    private final ItemRepository itemRepository;


    // 주문 ID로 주문 조회
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(NO_ORDER_NUMBER.getMessage()));
        return new OrderDto(order);
    }
    //동일한 이메일에 해당하는 모든 주문 조회
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByEmail(String email) {
        List<Order> orders = orderRepository.findByEmail(email);
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(); // createdAt 내림차순 정렬
        return orders.stream()
                .map(OrderDto::new) // Order -> OrderDto 변환
                .collect(Collectors.toList());
    }

    // 전체 주문 조회 또는 주문 상태에 따른 조회 후 DTO로 변환하여 반환
    @Transactional
    public List<OrderSummaryDto> getOrders(OrderStatus orderStatus) {
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

//    @Transactional(readOnly = true)
//    public AdminOrderDetailDto getOrderByEmail(String email) {
//        Order order = (Order) orderRepository.findFirstByEmailOrderByCreatedAtDesc(email)
//                .orElseThrow(() -> new IllegalArgumentException(NO_EMAIL.getMessage()));
//        return new AdminOrderDetailDto(order);
//    }


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
        Order existingOrder = orderRepository.findByIdOrderWithOrderItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NO_ORDER_NUMBER.getMessage()));

        existingOrder.setAddress(orderDetailDto.getAddress());
        existingOrder.setZipcode(orderDetailDto.getZipcode());

        Map<Long, OrderItem> existingItemMap = existingOrder.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));

        List<OrderItem> itemsToRemove = new ArrayList<>();
        List<OrderItem> itemsToAdd = new ArrayList<>();

        for (OrderDetailItemDto dto : orderDetailDto.getOrderItems()) {
            Item entityItem = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException(ITEM_NOT_FOUND.getMessage()));

            if (dto.getId() != null && existingItemMap.containsKey(dto.getId())) {
                OrderItem existingItem = existingItemMap.get(dto.getId());
                existingItem.setQuantity(dto.getQuantity());
                existingItemMap.remove(dto.getId());
            } else {
                OrderItem newItem = new OrderItem(existingOrder, entityItem, dto.getPrice(), dto.getQuantity(), dto.getImagePath());
                itemsToAdd.add(newItem);
            }
        }

        itemsToRemove.addAll(existingItemMap.values());

        existingOrder.getOrderItems().removeAll(itemsToRemove);

        existingOrder.getOrderItems().addAll(itemsToAdd);

        return new OrderDetailDto(existingOrder);
    }

    //이메일로 주문 유무 확인
    public boolean emailExists(String email) {
        return orderRepository.existsByEmail(email);
    }

    //주문 상태에 따라 데이터를 다르게 처리하여 뷰에 전달할 데이터 구성
    @Transactional
    public OrderListDto getGroupedOrdersByEmail(String email) {
        List<Order> orders = orderRepository.findByEmail(email);

        if (orders.isEmpty()) {
            throw new IllegalArgumentException(NO_EMAIL.getMessage() + email);
        }

        // RECEIVED 상태의 주문 통합
        List<Order> receivedOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.RECEIVED)
                .toList();
        OrderDto mergedReceivedOrder = mergeOrders(receivedOrders);


        // SHIPPING 상태의 주문을 날짜별로 그룹화하여 통합
        Map<LocalDate, OrderDto> shippingOrdersByDate = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.SHIPPING)
                .collect(Collectors.groupingBy(
                        order -> order.getModifiedAt().toLocalDate(),  // 수정된 날짜 기준으로 그룹화
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::mergeOrders // 해당 날짜의 주문들을 하나로 통합
                        )
                ));

        return new OrderListDto(mergedReceivedOrder, shippingOrdersByDate);
    }

    // 여러 개의 주문을 하나로 합치는 로직
    private OrderDto mergeOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            return null;
        }

        // 첫 번째 주문을 기준으로 사용
        Order baseOrder = orders.get(0);

        // 모든 주문의 총 가격 합산
        int totalPrice = orders.stream()
                .mapToInt(Order::getTotalPrice)
                .sum();

        // 해당 날짜의 주문 개수
        int orderCount = orders.size();

        // 첫 번째 주문의 첫 번째 아이템 이미지 경로
        String firstImagePath = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getImagePath)
                .filter(imagePath -> imagePath != null && !imagePath.isEmpty()) // 이미지가 있는 경우만
                .findFirst()
                .orElse(null);

        // 모든 주문 아이템을 하나의 리스트로 병합
        List<OrderItemDto> mergedOrderItems = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItemDto::new)
                .collect(Collectors.toList());

        return new OrderDto(
                baseOrder.getId(),
                baseOrder.getEmail(),
                baseOrder.getAddress(),
                baseOrder.getZipcode(),
                baseOrder.getOrderStatus(),
                totalPrice,
                baseOrder.getModifiedAt().toLocalDate(), // 주문 날짜 추가 (수정)
                firstImagePath, // 첫 번째 이미지 경로 추가
                orderCount, // 주문 개수 추가
                mergedOrderItems
        );
    }


    @Transactional
    public OrderDetailDto getOrderDetailDtoById(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));

        return new OrderDetailDto(order);
    }

    @Transactional
    public Order getOrderByIdWithItems(long orderId) {
        return orderRepository.findByIdOrderWithOrderItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
    }


}