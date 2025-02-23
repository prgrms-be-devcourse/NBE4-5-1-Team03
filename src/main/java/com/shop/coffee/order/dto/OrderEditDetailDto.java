package com.shop.coffee.order.dto;

import com.shop.coffee.item.entity.Item;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.dto.OrderItemEditDetailDto;
import com.shop.coffee.orderitem.entity.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class OrderEditDetailDto {

    private Long id;
    private String address;
    private String zipcode;
    private String email;
    private List<OrderItemEditDetailDto> orderItemEditDetailDtos;
    private int totalPrice;

    public OrderEditDetailDto(Order order, List<Item> allItems) {
        extractOrderInfo(order);
        this.orderItemEditDetailDtos = createOrderItemDtos(order, allItems);
    }

    private void extractOrderInfo(Order order) {
        this.id = order.getId();
        this.address = order.getAddress();
        this.zipcode = order.getZipcode();
        this.email = order.getEmail();
        this.totalPrice = order.getTotalPrice();
    }

    private List<OrderItemEditDetailDto> createOrderItemDtos(Order order, List<Item> allItems) {
        Map<Long, OrderItem> orderedItems = mapOrderedItems(order);
        return allItems.stream()
                .map(item -> createOrderItemDto(item, orderedItems))
                .collect(Collectors.toList());
    }

    private Map<Long, OrderItem> mapOrderedItems(Order order) {
        return order.getOrderItems().stream()
                .collect(Collectors.toMap(
                        orderItem -> orderItem.getItem().getId(),
                        orderItem -> orderItem
                ));
    }

    private OrderItemEditDetailDto createOrderItemDto(Item item, Map<Long, OrderItem> orderedItems) {
        OrderItem orderItem = orderedItems.get(item.getId());
        return orderItem != null ? new OrderItemEditDetailDto(orderItem) : new OrderItemEditDetailDto(item);
    }
}
