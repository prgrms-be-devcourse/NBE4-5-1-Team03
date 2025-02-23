package com.shop.coffee.order.service;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.service.ItemService;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.*;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.dto.OrderItemEditDetailDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.service.OrderItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.shop.coffee.global.exception.ErrorCode.NOSINGLEORDER;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ItemService itemService;
  
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
    public OrderDetailDto updateOrder(Long orderId, OrderEditDetailDto orderEditDetailDto) {
        Order existingOrder = orderRepository.findByIdFetchOrderItemsAndItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException(NOSINGLEORDER.getMessage()));

        existingOrder.setAddress(orderEditDetailDto.getAddress());
        existingOrder.setZipcode(orderEditDetailDto.getZipcode());

        Map<Long, OrderItem> existingItemMap = existingOrder.getOrderItems().stream()
                .collect(Collectors.toMap(orderItem -> orderItem.getItem().getId(), item -> item));

        List<OrderItem> itemsToRemove = new ArrayList<>();
        List<OrderItem> itemsToAdd = new ArrayList<>();

        for (OrderItemEditDetailDto dto : orderEditDetailDto.getOrderItemEditDetailDtos()) {
            Item entityItem = itemService.getItemByIdEntity(dto.getItemId());

            if (existingItemMap.containsKey(dto.getItemId())) {
                // 기존 주문에 있는 상품이면 수량 변경
                OrderItem existingItem = existingItemMap.get(dto.getItemId());

                if (dto.getQuantity() == 0) {
                    // 수량이 0이면 삭제 예정 리스트에 추가
                    itemsToRemove.add(existingItem);
                } else {
                    // 수량 업데이트
                    existingItem.setQuantity(dto.getQuantity());
                }

                existingItemMap.remove(dto.getItemId());
            } else {
                // 새로운 상품 추가
                if (dto.getQuantity() > 0) {
                    OrderItem newItem = new OrderItem(existingOrder, entityItem, dto.getPrice(), dto.getQuantity(), dto.getImagePath());
                    itemsToAdd.add(newItem);
                }
            }
        }

        // 기존 주문에서 삭제할 상품 제거
        existingOrder.getOrderItems().removeAll(itemsToRemove);

        // 새로운 상품 추가
        existingOrder.getOrderItems().addAll(itemsToAdd);

        // 총 주문 금액 다시 계산 (서버에서 직접 계산)
        int newTotalPrice = existingOrder.getOrderItems().stream()
                .mapToInt(item -> item.getQuantity() * item.getPrice())
                .sum();
        existingOrder.setTotalPrice(newTotalPrice);

        return new OrderDetailDto(existingOrder); // 확인하기 위한 상세보기 DTO(수정 DTO는 List<Item>이 있어야 되기 떄문에 사용하지 않음)
    }

    //이메일로 주문 유무 확인
    public boolean emailExists(String email) {
        return orderRepository.existsByEmail(email);
    }

    @Transactional
    public OrderDetailDto getOrderDetailDtoById(long orderId) {
        Order order = orderRepository.findByIdFetchOrderItemsAndItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
        order.getOrderItems().sort(Comparator.comparing(orderItem -> orderItem.getItem().getName())); // 이름순 정렬
        return new OrderDetailDto(order);
    }

    @Transactional
    public OrderEditDetailDto getOrderEditDetailDtoById(long orderId) {
        Order order = orderRepository.findByIdFetchOrderItemsAndItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOSINGLEORDER.getMessage()));
        List<Item> allItems = itemService.getAllItemEntities();
        order.getOrderItems().sort(Comparator.comparing(orderItem -> orderItem.getItem().getName())); // 이름순 정렬
        return new OrderEditDetailDto(order, allItems);
    }
}