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


    public OrderPaymentRequestDto(    @JsonProperty("email") @NotBlank String email,
                                      @JsonProperty("address") @NotBlank String address,
                                      @JsonProperty("zipCode") @NotBlank String zipCode,
                                      @JsonProperty("items") @NotEmpty List<ItemToOrderItemDto> items) {
        this.email = email;
        this.address = address;
        this.zipCode = zipCode;
        this.items = items;
    }
}
