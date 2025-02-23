package com.shop.coffee.orderitem.service;

import com.shop.coffee.item.dto.ItemToOrderItemDto;
import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.repository.ItemRepository;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.entity.OrderItem;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.shop.coffee.global.exception.ErrorCode.NO_SINGLE_ITEM;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public List<OrderItem> createListItem(Order newOrder, List<ItemToOrderItemDto> items) {

        for(ItemToOrderItemDto item: items) {
            Item entityItem = itemRepository.findById(item.getId())
                    .orElseThrow(() -> new EntityNotFoundException(NO_SINGLE_ITEM.getMessage()));

            newOrder.addOrderItem(new OrderItem(newOrder, entityItem, item.getPrice(), item.getQuantity(), item.getImagePath()));
        }
        newOrder.setTotalPrice(calculateTotalPrice(newOrder.getOrderItems()));
        return newOrder.getOrderItems();
    }

    private int calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
