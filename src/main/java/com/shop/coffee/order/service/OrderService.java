package com.shop.coffee.order.service;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.item.service.ItemService;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.*;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.dto.OrderItemEditDetailDto;
import com.shop.coffee.orderitem.dto.OrderItemIntegrationDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.service.OrderItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.shop.coffee.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    //주문 번호로 주문 단건 조회
    private final OrderItemService orderItemService;
    private final ItemService itemService;
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
            orders = orderRepository.findAllByOrderByModifiedAtDesc(); // 전체 주문 조회
        } else {
            orders = orderRepository.findByOrderStatusOrderByModifiedAtDesc(orderStatus); // 주문 상태에 따른 조회
        }
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        return orders.stream().map(com.shop.coffee.order.dto.OrderSummaryDto::new).collect(Collectors.toList());
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

        int orderItemsCnt = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToInt(OrderItem::getQuantity)  // 각 주문 아이템의 수량을 가져와 합산
                .sum();


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
                mergedOrderItems, // 병합된 주문 아이템 리스트 추가
                orderItemsCnt
        );
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