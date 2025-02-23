package com.shop.coffee.order.service;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.*;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.dto.OrderDetailItemDto;
import com.shop.coffee.orderitem.dto.OrderItemIntegrationDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.service.OrderItemService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.shop.coffee.global.exception.ErrorCode.ITEM_NOT_FOUND;
import static com.shop.coffee.global.exception.ErrorCode.NOSINGLEORDER;
import static com.shop.coffee.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ItemRepository itemRepository;
  
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
        return new OrderDto(order);
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
            throw new EntityNotFoundException(NOSINGLEORDER.getMessage());
        }
        return orders.stream().map(OrderSummaryDto::new).collect(Collectors.toList());
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
        Order existingOrder = orderRepository.findByIdOrderWithOrderItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOSINGLEORDER.getMessage()));

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

    /**
     * 주문 통합
     * @param oldOrderId 기존 주문의 아이디
     * @param newOrderDto 신규 주문
     * @param selectedLocation 선택된 배송지
     */
    @Transactional
    public void integrateOrders(
            Long oldOrderId,
            OrderIntegrationDto newOrderDto,
            String selectedLocation) {

        // 변경 감지를 위해 기존 주문 조회
        Order oldOrder = orderRepository.findById(oldOrderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND.getMessage()));

        // 기존 주문 정보로부터 맵 생성
        Map<Long, OrderItem> oldOrderItemMap = mapOrderItems(oldOrder);

        // 신규 주문 통합 처리
        processNewOrderItems(oldOrder, newOrderDto, oldOrderItemMap);

        // 기존 주문에 변경된 배송지 반영
        if (!selectedLocation.equals("oldOrderLocation")) {
            updateOrderLocation(oldOrder, newOrderDto);
        }

        // 총 결제 금액 합산
        updateTotalPrice(oldOrder, newOrderDto);
    }

    /**
     * key가 itemId, value가 OrderItem인 맵 생성
     * @param order 주문
     * @return 주문 관련 정보를 저장하는 맵
     */
    private Map<Long, OrderItem> mapOrderItems(Order order) {
        Map<Long, OrderItem> orderItemMap = new HashMap<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            orderItemMap.put(orderItem.getItem().getId(), orderItem);
        }

        return orderItemMap;
    }

    /**
     * 신규 주문 통합 처리
     * @param oldOrder 기존 주문
     * @param newOrderDto 신규 주문
     * @param oldOrderItemMap 기존 주문 관련 맵
     */
    private void processNewOrderItems(
            Order oldOrder,
            OrderIntegrationDto newOrderDto,
            Map<Long, OrderItem> oldOrderItemMap) {

        // 신규 주문의 주문 상품 목록 순회
        for (OrderItemIntegrationDto newOrderItemDto : newOrderDto.getOrderItems()) {
            Long newItemId = newOrderItemDto.getItem().getId();

            if (oldOrderItemMap.containsKey(newItemId)) { // 기존 주문에 포함된 주문 상품인 경우
                OrderItem oldOrderItem = oldOrderItemMap.get(newItemId);
                oldOrderItem.addQuantity(newOrderItemDto.getQuantity()); // 수량 추가
            } else { // 기존 주문에 포함되지 않은 주문 상품인 경우
                addOrderItemToOrder(oldOrder, newOrderItemDto); // 새로운 주문 상품 추가
            }
        }
    }

    /**
     * 기존 주문에 신규 주문 상품 추가
     * @param oldOrder 기존 주문
     * @param newOrderItemDto 신규 주문
     */
    private void addOrderItemToOrder(Order oldOrder, OrderItemIntegrationDto newOrderItemDto) {
        Item item = itemRepository.findById(newOrderItemDto.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException(ITEM_NOT_FOUND.getMessage()));

        OrderItem newOrderItem = new OrderItem(oldOrder, item, newOrderItemDto.getPrice(),
                newOrderItemDto.getQuantity(), newOrderItemDto.getImagePath());

        oldOrder.addOrderItem(newOrderItem);
    }

    /**
     * 배송지 변경
     * @param oldOrder 기존 주문
     * @param newOrderDto 신규 주문
     */
    private void updateOrderLocation(Order oldOrder, OrderIntegrationDto newOrderDto) {
        oldOrder.updateLocation(newOrderDto.getAddress(), newOrderDto.getZipcode());
    }

    /**
     * 결제 금액 합산
     * @param oldOrder 기존 주문
     * @param newOrderDto 신규 주문
     */
    private void updateTotalPrice(Order oldOrder, OrderIntegrationDto newOrderDto) {
        oldOrder.addTotalPrice(newOrderDto.getTotalPrice());
    }

}