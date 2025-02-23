package com.shop.coffee.order.dto;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDto {
    private Long id;
    private String email;
    private String address;
    private String zipcode;
    private String orderStatus;
    private int totalPrice;
    private LocalDate orderDate; //  주문 날짜
    private String firstImagePath; // 첫 번째 주문 아이템 이미지
    private int orderCount; // 해당 날짜의 주문 개수
    private List<OrderItemDto> orderItems; // 주문 아이템 리스트

    public OrderDto(Order order) {
        this.id = order.getId();
        this.email = order.getEmail();
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.orderStatus = order.getOrderStatus().name();
        this.totalPrice = order.getTotalPrice();
        this.orderDate = order.getModifiedAt().toLocalDate(); // 주문 최종 수정 날짜
        this.firstImagePath = order.getOrderItems().isEmpty() ? null : order.getOrderItems().get(0).getImagePath(); // 첫 번째 이미지 경로
        this.orderCount = 1; // 개별 주문은 1
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }
    // 통합된 주문용 생성자
    public OrderDto(Long id, String email, String address, String zipcode, OrderStatus orderStatus, int totalPrice,
                    LocalDate orderDate, String firstImagePath, int orderCount, List<OrderItemDto> orderItems) {
        this.id = id;
        this.email = email;
        this.address = address;
        this.zipcode = zipcode;
        this.orderStatus = orderStatus.name();
        this.totalPrice = totalPrice;//
        this.orderDate = orderDate;
        this.firstImagePath = firstImagePath;
        this.orderCount = orderCount;
        this.orderItems = orderItems;
    }

}

