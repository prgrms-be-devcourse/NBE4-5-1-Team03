package com.shop.coffee.order.dto;

import com.shop.coffee.order.entity.Order;
import lombok.Getter;

@Getter
public class OrderDto {
    private Long id;
    private String email;
    private String address;
    private String zipcode;
    private String orderStatus;
    private int totalPrice;

    public OrderDto(Order order) {
        this.id = order.getId();
        this.email = order.getEmail();
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.orderStatus = order.getOrderStatus().name();
        this.totalPrice = order.getTotalPrice();
    }
}
