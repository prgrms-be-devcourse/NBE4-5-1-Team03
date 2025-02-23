package com.shop.coffee.order.dto;

import lombok.Getter;

@Getter
public class OrderIntegrationViewDto {

    private final String viewName;
    private final OrderIntegrationDto oldOrder;
    private final OrderIntegrationDto newOrder;

    public OrderIntegrationViewDto(String viewName, OrderIntegrationDto oldOrder, OrderIntegrationDto newOrder) {
        this.viewName = viewName;
        this.oldOrder = oldOrder;
        this.newOrder = newOrder;
    }
}
