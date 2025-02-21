package com.shop.coffee.order.dto;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import lombok.Getter;

@Getter
public class OrderSummaryDTO {
    private final OrderStatus orderStatus;
    private final String email;
    private final String modifiedAt;
    private final int totalPrice;
    private final String SummaryItemName;

    public OrderSummaryDTO(Order order) {
        this.orderStatus = order.getOrderStatus();
        this.email = order.getEmail();
        this.modifiedAt = String.valueOf(order.getModifiedAt());
        this.totalPrice = order.getTotalPrice();
        this.SummaryItemName = getSummaryItemName(order);
    }

    private String getSummaryItemName(Order order) {
        if (order.getOrderItems().size() == 1) {
            return order.getOrderItems().get(0).getItem().getName();
        } else {
            return order.getOrderItems().get(0).getItem().getName() + " 외 " + (order.getOrderItems().size() - 1) + "건";
        }
    }
}