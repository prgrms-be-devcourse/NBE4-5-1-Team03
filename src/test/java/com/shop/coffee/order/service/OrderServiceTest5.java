package com.shop.coffee.order.service;

import com.shop.coffee.item.entity.Item;
import com.shop.coffee.item.service.ItemService;
import com.shop.coffee.order.dto.OrderDetailDto;
import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.dto.OrderEditDetailDto;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.orderitem.entity.OrderItem;
import com.shop.coffee.orderitem.repository.OrderItemRepository;
import com.shop.coffee.orderitem.service.OrderItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = com.shop.coffee.CoffeeApplication.class)
@Transactional
public class OrderServiceTest5 {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ItemService itemService;

    @Test
    @DisplayName("주문 생성시 정상작동 테스트")
    public void testCreateOrder() {
        String email = "test@test";
        String address = "서울특별시 A";
        String zipCode = "12345";
        OrderItem orderItem = new OrderItem();

        Order expectedOrder = new Order(email, address, zipCode, Collections.singletonList(orderItem));

        Order result = orderService.create(email, address, zipCode, Collections.singletonList(orderItem));

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getAddress()).isEqualTo(address);
        assertThat(result.getZipcode()).isEqualTo(zipCode);
        assertThat(result.getOrderItems().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 조회 후 존재 확인 및 삭제 후 getOrderById 호출 시 예외 발생 테스트")
    public void testDeleteOrder() {
        long orderId = 1L;

        OrderDto orderDto = orderService.getOrderById(orderId);
        assertThat(orderDto).isNotNull();

        orderService.deleteOrder(orderId);

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById(orderId);
        });
    }

    @Test
    @DisplayName("주문이 존재할 경우 정상적으로 업데이트되는지 테스트")
    public void testUpdateOrder_Success() {
        long orderId = 1L;

        OrderItem orderItem1 = orderItemRepository.findById(1L).get();
        OrderItem orderItem2 = orderItemRepository.findById(2L).get();
        List<Item> allItems = itemService.getAllItemEntities();
        Order order = orderService.create("test@test.com","부산광역시 A123", "90123", List.of(orderItem1, orderItem2)); // 여기서 필요한 필드를 세팅해야 함

        OrderEditDetailDto orderRequestDto = new OrderEditDetailDto(order, allItems);
        OrderDetailDto updateRequestDto = orderService.updateOrder(orderId, orderRequestDto);

        assertThat(updateRequestDto.getAddress()).isEqualTo("부산광역시 A123");
        assertThat(updateRequestDto.getZipcode()).isEqualTo("90123");
        assertThat(updateRequestDto.getOrderItems().size()).isEqualTo(2);
    }
}
