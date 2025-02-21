package com.shop.coffee.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.entity.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderPaymentRequestDto {

    private final String email;
    private final String address;
    private final String zipCode;
    private final List<OrderItem> orderItems;

    public OrderPaymentRequestDto(    @JsonProperty("email") @NotBlank String email,
                                      @JsonProperty("address") @NotBlank String address,
                                      @JsonProperty("zipCode") @NotBlank String zipCode,
                                      @JsonProperty("orderItems") @NotEmpty List<OrderItem> orderItems) {
        this.email = email;
        this.address = address;
        this.zipCode = zipCode;
        this.orderItems = orderItems;
    }
}
