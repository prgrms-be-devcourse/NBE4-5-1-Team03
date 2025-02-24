package com.shop.coffee.order.dto;

import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.dto.OrderItemIntegrationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class OrderIntegrationDto {

    private Long id;
    private String email;
    private String address;
    private String zipcode;
    private int totalPrice;
    private List<OrderItemIntegrationDto> orderItems;

    public OrderIntegrationDto(Order order) {
        this.id = order.getId();
        this.email = order.getEmail();
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.totalPrice = order.getTotalPrice();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemIntegrationDto::new)
                .collect(Collectors.toList());
    }
}