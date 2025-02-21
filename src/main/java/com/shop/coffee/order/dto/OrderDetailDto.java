package com.shop.coffee.order.dto;

import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderDetailDto {
    private String address;
    private String zipcode;
    private LocalDateTime modifiedAt;
    private int[][] productInfo;
    private int totalPrice;

    public OrderDetailDto(Order order) {
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.modifiedAt = order.getModifiedAt();
        this.totalPrice = order.getTotalPrice();
        this.productInfo = new int[4][2]; // Initialize 4x2 array with zeros

        for (OrderItem item : order.getOrderItems()) {
            int itemId = item.getItem().getId().intValue() - 1; // Assuming item IDs are 1-based and sequential
            if (itemId >= 0 && itemId < 4) {
                this.productInfo[itemId][0] = item.getPrice();
                this.productInfo[itemId][1] = item.getQuantity();
            }
        }
    }
}