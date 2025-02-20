package com.shop.coffee.order.service;

import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

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
}
