package com.shop.coffee.orderitem.dto;

import com.shop.coffee.item.dto.ItemIntegrationDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;

@Getter
public class OrderItemIntegrationDto {

    private Long id;
    private ItemIntegrationDto item;
    private int price;
    private int quantity;
    private String imagePath;

    public OrderItemIntegrationDto(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.item = new ItemIntegrationDto(orderItem.getItem());
        this.price = orderItem.getPrice();
        this.quantity = orderItem.getQuantity();
        this.imagePath = orderItem.getImagePath();
    }
}
