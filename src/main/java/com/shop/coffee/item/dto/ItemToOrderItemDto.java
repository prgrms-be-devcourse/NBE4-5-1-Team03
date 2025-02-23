package com.shop.coffee.item.dto;

import com.shop.coffee.item.entity.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemToOrderItemDto {
    private Long id;
    private String name;
    private int price;
    private int quantity;
    private String imagePath;

    public ItemToOrderItemDto() {}

    public ItemToOrderItemDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.imagePath = item.getImagePath();
    }

    public ItemToOrderItemDto(Item item, int quantity) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.imagePath = item.getImagePath();
        this.quantity = quantity;
    }
}
