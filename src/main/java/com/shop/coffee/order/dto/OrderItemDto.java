package com.shop.coffee.order.dto;

import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;

//OrderItemDto는 주문 상품을 나타내는 DTO입니다.
@Getter
public class OrderItemDto {
    private String itemName;
//    private Long productId;
//    private String productName;
    private long price;
    private int quantity;
    private String imagePath; // ✅ 이미지 경로 추가

    // OrderItem을 받는 생성자
    public OrderItemDto(OrderItem orderItem) {
        this.itemName = orderItem.getItem().getName();
        this.price = orderItem.getPrice();
        this.quantity = orderItem.getQuantity();
        this.imagePath = orderItem.getImagePath();
    }
}
