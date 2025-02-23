package com.shop.coffee.order.dto;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import lombok.Getter;

@Getter
public class OrderSummaryDto {
    private final Long id;
    private final OrderStatus orderStatus;
    private final String email;
    private final String modifiedAt;
    private final int totalPrice;
    private final String summaryItemName;
    private final String summaryImagePath;

    public OrderSummaryDto(Order order) {
        this.id = order.getId();
        this.orderStatus = order.getOrderStatus();
        this.email = order.getEmail();
        this.modifiedAt = String.valueOf(order.getModifiedAt());
        this.totalPrice = order.getTotalPrice();
        this.summaryItemName = getSummaryItemName(order);
        this.summaryImagePath = getSummaryImagePath(order);
    }

    private String getSummaryItemName(Order order) {
        if (order.getOrderItems().size() == 1) {
            return order.getOrderItems().getFirst().getItem().getName();
        } else {
            return order.getOrderItems().getFirst().getItem().getName() + " 외 " + (order.getOrderItems().size() - 1) + "건";
        }
    }

    private String getSummaryImagePath(Order order) {
        if (!order.getOrderItems().isEmpty() && order.getOrderItems().getFirst().getImagePath() != null) {
            return order.getOrderItems().getFirst().getImagePath();
        }
        return null;
    }
}