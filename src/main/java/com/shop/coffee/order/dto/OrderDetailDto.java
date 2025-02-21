package com.shop.coffee.order.dto;

import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderDetailDto {
    private Long id;
    private String email;
    private String address;
    private String zipcode;
    private String orderStatus;
    private int totalPrice;
    private List<OrderItem> orderItems;

    public OrderDetailDto(Order order) {
        this.id = order.getId();
        this.email = order.getEmail();
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.orderStatus = order.getOrderStatus().name();
        this.totalPrice = order.getTotalPrice();
        this.orderItems = order.getOrderItems();
    }
}
