package com.shop.coffee.order.service;

import com.shop.coffee.item.entity.Item;
import com.shop.coffee.order.OrderStatus;
import com.shop.coffee.order.dto.OrderSummaryDto;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.orderitem.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ActiveProfiles("local")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest2 {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        Item item1 = new Item("AAA", "X", 500, "맛있음", null);
        Item item2 = new Item("BBB", "Y", 300, "맛없음", null);

        order1 = new Order("test1@example.com", "123 Street", "12345", OrderStatus.RECEIVED, 1000, null);
        order2 = new Order("test2@example.com", "456 Avenue", "67890", OrderStatus.SHIPPING, 300, null);

        OrderItem orderItem1 = new OrderItem(order1, item1, 500, 2, null);
        OrderItem orderItem2 = new OrderItem(order2, item2, 300, 1, null);

        order1.setOrderItems(List.of(orderItem1));
        order2.setOrderItems(List.of(orderItem2));

        // Mock 설정
        lenient().when(orderRepository.findAllByOrderByModifiedAtDesc()).thenReturn(Arrays.asList(order1, order2));
        lenient().when(orderRepository.findByOrderStatusOrderByModifiedAtDesc(OrderStatus.RECEIVED)).thenReturn(Collections.singletonList(order1));
    }

    @Test
    @DisplayName("모든 주문 조회")
    void test1() {

        //  when
        //  getOrders의 인자로 null이 왔으므로 모든 주문 조회
        List<OrderSummaryDto> orders = orderService.getOrders(null);

        // then
        // 2개의 주문이 조회되어야 함
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    @DisplayName("주문 상태가 RECEIVED인 주문 조회")
    void test2() {

        // when
        // getOrders의 인자로 OrderStatus.RECEIVED가 왔으므로 상태가 RECEIVED인 주문 조회
        List<OrderSummaryDto> orders = orderService.getOrders(OrderStatus.RECEIVED);

        // then
        // 1개의 주문이 조회되어야 함
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals("주문 접수", orders.get(0).getOrderStatus());
    }

    @Test
    @DisplayName("주문이 없는 경우 빈 리스트 반환")
    void test3() {
        // given
        // 주문이 없는 경우
        when(orderRepository.findAllByOrderByModifiedAtDesc()).thenReturn(Collections.emptyList());

        // when & then
        // getOrders의 인자로 null이 왔으므로 모든 주문 조회 시 빈 리스트
        assertEquals(0, orderService.getOrders(null).size());
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void test4() {
        // given
        String existingEmail = "test1@example.com";
        String nonExistingEmail = "nonexistent@example.com";

        // Mock 설정
        when(orderRepository.existsByEmail(existingEmail)).thenReturn(true);
        when(orderRepository.existsByEmail(nonExistingEmail)).thenReturn(false);

        // when
        boolean exists = orderService.emailExists(existingEmail);
        boolean notExists = orderService.emailExists(nonExistingEmail);

        // then
        assertTrue(exists);
        assertFalse(notExists);
    }
}