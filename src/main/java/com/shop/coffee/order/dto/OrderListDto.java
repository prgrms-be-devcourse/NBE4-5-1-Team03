package com.shop.coffee.order.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

// OrderListDto는 주문 목록을 나타내는 DTO입니다.
@Getter
public class OrderListDto {
    private final OrderDto mergedReceivedOrder;  // RECEIVED 상태 주문 통합 데이터
    private final Map<LocalDate, OrderDto> shippingOrdersByDate; // 날짜별 SHIPPING 주문

    public OrderListDto(OrderDto mergedReceivedOrder, Map<LocalDate, OrderDto> shippingOrdersByDate) {
        this.mergedReceivedOrder = mergedReceivedOrder;
        this.shippingOrdersByDate = shippingOrdersByDate;
    }

    public OrderDto getMergedReceivedOrder() {
        return mergedReceivedOrder;
    }

    public Map<LocalDate, OrderDto> getShippingOrdersByDate() {
        return shippingOrdersByDate;
    }
}
