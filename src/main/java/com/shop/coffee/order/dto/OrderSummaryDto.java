package com.shop.coffee.order.dto;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class OrderSummaryDto {
    private final Long id;
    private final String orderStatus;
    private final String email;
    private final int totalPrice;
    private final String summaryItemName;
    private final String summaryImagePath;
    private final String shippingStartDate;

    public OrderSummaryDto(Order order) {
        this.id = order.getId();
        this.orderStatus = ConvertOrderStatus(order.getOrderStatus());
        this.email = order.getEmail();
        this.totalPrice = order.getTotalPrice();
        this.summaryItemName = getSummaryItemName(order);
        this.summaryImagePath = getSummaryImagePath(order);
        this.shippingStartDate = calculateShippingStartDate(order.getCreatedAt());
    }

    private String getSummaryItemName(Order order) {
        int totalItemCount = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (totalItemCount == 1) {
            return order.getOrderItems().getFirst().getItem().getName();
        } else {
            return order.getOrderItems().getFirst().getItem().getName() + " 외 " + (totalItemCount - 1) + "건";
        }
    }

    private String getSummaryImagePath(Order order) {
        if (!order.getOrderItems().isEmpty() && order.getOrderItems().getFirst().getImagePath() != null) {
            return order.getOrderItems().getFirst().getImagePath();
        }
        return null;
    }

    private String calculateShippingStartDate(LocalDateTime createdAt) {
        if(createdAt == null) {
            return null;
        }

        LocalDateTime cutoffTime = createdAt.toLocalDate().atTime(14, 0);

        LocalDateTime shippingDate = createdAt.isAfter(cutoffTime)
                ? createdAt.plusDays(1)
                : createdAt;

        return shippingDate.format(DateTimeFormatter.ofPattern("MM. dd(E)"));
    }

    private String ConvertOrderStatus(OrderStatus orderStatus) {
        return orderStatus == OrderStatus.SHIPPING ? "배송 시작" : "주문 접수";
    }
}