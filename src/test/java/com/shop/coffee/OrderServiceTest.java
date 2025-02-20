package com.shop.coffee;

import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        order1 = new Order("test1@example.com", "123 Street", "12345", OrderStatus.RECEIVED, 1000, new ArrayList<>());
        order2 = new Order("test2@example.com", "456 Avenue", "67890", OrderStatus.SHIPPING, 2000, new ArrayList<>());

        orderRepository.save(order1);
        orderRepository.save(order2);
    }

    @Test
    @DisplayName("모든 주문 조회")
    void test1() {
        // given
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // when
        List<Order> orders = orderService.getOrders(null);

        // then
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    @DisplayName("주문 상태가 RECEIVED인 주문 조회")
    void test2() {
        // given
        when(orderRepository.findByOrderStatus(OrderStatus.RECEIVED)).thenReturn(Arrays.asList(order1));

        // when
        List<Order> orders = orderService.getOrders(OrderStatus.RECEIVED);

        // then
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.RECEIVED, orders.get(0).getOrderStatus());
    }
}