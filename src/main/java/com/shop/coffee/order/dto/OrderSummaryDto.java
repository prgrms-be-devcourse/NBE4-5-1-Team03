package com.shop.coffee.order.dto;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class OrderSummaryDto {
    private final Long id;
    private final OrderStatus orderStatus;
    private final String email;
    private final int totalPrice;
    private final String summaryItemName;
    private final String summaryImagePath;
    private final String shippingStartDate;

    public OrderSummaryDto(Order order) {
        this.id = order.getId();
        this.orderStatus = order.getOrderStatus();
        this.email = order.getEmail();
        this.totalPrice = order.getTotalPrice();
        this.summaryItemName = getSummaryItemName(order);
        this.summaryImagePath = getSummaryImagePath(order);
        this.shippingStartDate = calculateShippingStartDate(order.getModifiedAt());
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

    private String calculateShippingStartDate(LocalDateTime modifiedAt) {
        if(modifiedAt == null) {
            return null;
        }

        LocalDateTime cutoffTime = modifiedAt.toLocalDate().atTime(14, 0);

        LocalDateTime shippingDate = modifiedAt.isAfter(cutoffTime)
                ? modifiedAt.plusDays(1)
                : modifiedAt;

        return shippingDate.format(DateTimeFormatter.ofPattern("MM. dd(E)"));
    }
}