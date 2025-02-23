package com.shop.coffee.orderitem.dto;

import com.shop.coffee.item.dto.ItemDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailItemDto {

    private Long id;

    private Long itemId;

    private String itemName;

    private int price;

    private int quantity;

    private String imagePath;

    public OrderDetailItemDto(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.itemId = orderItem.getItem().getId();
        this.itemName = orderItem.getItem().getName();
        this.price = orderItem.getPrice();
        this.quantity = orderItem.getQuantity();
        this.imagePath = orderItem.getImagePath();
    }
}
