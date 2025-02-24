package com.shop.coffee.orderitem.dto;

import com.shop.coffee.item.entity.Item;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemEditDetailDto {
    private Long itemId;
    private String itemName;
    private int quantity;
    private int price;
    private int totalPrice;
    private String imagePath;

    // 기존 주문 상품용 생성자
    public OrderItemEditDetailDto(OrderItem orderItem) {
        this.itemId = orderItem.getItem().getId();
        this.itemName = orderItem.getItem().getName();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
        this.totalPrice = quantity * price;
        this.imagePath = orderItem.getImagePath();
    }

    // 새 상품용 생성자
    public OrderItemEditDetailDto(Item item) {
        this.itemId = item.getId();
        this.itemName = item.getName();
        this.quantity = 0;
        this.price = item.getPrice();
        this.totalPrice = 0;
        this.imagePath = item.getImagePath();
    }
}
