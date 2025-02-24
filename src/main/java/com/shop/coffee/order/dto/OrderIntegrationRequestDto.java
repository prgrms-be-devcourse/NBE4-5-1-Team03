package com.shop.coffee.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class OrderIntegrationRequestDto {
    private OrderIntegrationDto oldOrder;
    private OrderIntegrationDto newOrder;
    private String selectedLocation = "oldOrderLocation";
}