package com.shop.coffee.order.dto;

import com.shop.coffee.order.entity.Order;
import lombok.Getter;

@Getter
public class OrderIntegrationViewDto {

    private final String viewName;
    private final Order oldOrder;
    private final Order newOrder;

    public OrderIntegrationViewDto(String viewName, Order oldOrder, Order newOrder) {
        this.viewName = viewName;
        this.oldOrder = oldOrder;
        this.newOrder = newOrder;
    }
}
