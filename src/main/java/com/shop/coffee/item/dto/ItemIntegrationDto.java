package com.shop.coffee.item.dto;

import com.shop.coffee.item.entity.Item;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ItemIntegrationDto {
    private Long id;
    private String name;
    private String category;
    private int price;

    public ItemIntegrationDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.category = item.getCategory();
        this.price = item.getPrice();
    }
}