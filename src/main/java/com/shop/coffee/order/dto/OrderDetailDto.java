package com.shop.coffee.order.dto;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.dto.OrderDetailItemDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDetailDto {
    private Long id;
    private String email;
    private String address;
    private String zipcode;
    private OrderStatus orderStatus;
    private int totalPrice;
    private List<OrderDetailItemDto> orderItems;
    private String shippingStartDate;

    public OrderDetailDto(Order order) {
        this.id = order.getId();
        this.email = order.getEmail();
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.orderStatus = order.getOrderStatus();
        this.totalPrice = order.getTotalPrice();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderDetailItemDto::new)
                .collect(Collectors.toList());
        this.shippingStartDate = calculateShippingStartDate(order.getCreatedAt());
    }

    private String calculateShippingStartDate(LocalDateTime createdAt) {
        LocalDateTime cutoffTime = createdAt.toLocalDate().atTime(14, 0);

        LocalDateTime shippingDate = createdAt.isAfter(cutoffTime)
                ? createdAt.plusDays(1)
                : createdAt;

        return shippingDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
