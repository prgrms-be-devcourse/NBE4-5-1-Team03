package com.shop.coffee.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shop.coffee.item.dto.ItemToOrderItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderPaymentRequestDto {

    private String email;
    private String address;
    private String zipCode;
    private List<ItemToOrderItemDto> items;

    public OrderPaymentRequestDto() {
    }


    public OrderPaymentRequestDto(    @NotBlank String email,
                                      @NotBlank String address,
                                      @NotBlank String zipCode,
                                      @NotEmpty List<ItemToOrderItemDto> items) {
        this.email = email;
        this.address = address;
        this.zipCode = zipCode;
        this.items = items;
    }
}
