package com.shop.coffee.item.dto;

import com.shop.coffee.item.entity.Item;
import lombok.Getter;

@Getter
public class ItemDto {
    private Long id;
    private String name;
    private String category;
    private int price;
    private String description;
    private String imagePath;

    // Item 엔티티로부터 Dto로 변환하는 생성자
    public ItemDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.category = item.getCategory();
        this.price = item.getPrice();
        this.description = item.getDescription();
        this.imagePath = item.getImagePath();
    }
}
