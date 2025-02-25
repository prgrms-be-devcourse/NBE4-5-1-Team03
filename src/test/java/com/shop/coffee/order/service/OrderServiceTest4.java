package com.shop.coffee.order.service;

import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.dto.OrderListDto;
import com.shop.coffee.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("local")
@SpringBootTest(classes = com.shop.coffee.CoffeeApplication.class)
@Transactional
class OrderServiceTest4 {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("이메일로 주문 단건 조회 테스트")
    void getOrdersByEmailTest() {
        // given
        String email = "user3@example.com"; //Datainitializer에서 생성한 주문 데이터로 테스트

        // when
        List<OrderDto> orders = orderService.getOrdersByEmail(email);

        // then
        assertThat(orders).isNotEmpty();  // 주문이 존재하는지 확인
        assertThat(orders.size()).isEqualTo(1); // 해당 이메일에 대한 주문 개수 확인(user2@example.com의 주문은 1개)
        assertThat(orders.get(0).getEmail()).isEqualTo(email); // 이메일이 정확한지 확인
        assertThat(orders.get(0).getTotalPrice()).isEqualTo(1000); // 총 가격 확인
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 주문 조회 시 빈 리스트 반환")
    void getOrdersByNonExistentEmailTest() {
        // given
        String email = "nonexistent@example.com";

        // when
        List<OrderDto> orders = orderService.getOrdersByEmail(email);

        // then
        assertThat(orders).isEmpty(); // 주문이 존재하지 않아야 함
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
    @DisplayName("이메일로 주문 목록 조회 - RECEIVED 상태 주문이 정상적으로 통합되는지 확인")
    void testReceivedOrdersGrouping() {
        // given
        String email = "user5@example.com";

        // when
        OrderListDto orderListDto = orderService.getGroupedOrdersByEmail(email);

        // then
        OrderDto receivedOrder = orderListDto.getMergedReceivedOrder();
        assertThat(receivedOrder).isNotNull();
        assertThat(receivedOrder.getOrderStatus()).isEqualTo("RECEIVED");

        // RECEIVED 상태의 주문 총 가격이 정상적으로 합산되는지 확인
        assertThat(receivedOrder.getTotalPrice()).isEqualTo(100 + 500);
        // 주문 개수 확인
        assertThat(receivedOrder.getOrderCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("이메일로 주문 목록 조회 - SHIPPING 상태 주문이 날짜별로 그룹화되는지 확인")
    void testShippingOrdersGrouping() {
        // given
        String email = "user5@example.com";

        // when
        OrderListDto orderListDto = orderService.getGroupedOrdersByEmail(email);

        // then
        Map<LocalDate, OrderDto> shippingOrdersByDate = orderListDto.getShippingOrdersByDate();
        assertThat(shippingOrdersByDate).isNotEmpty();
        assertThat(shippingOrdersByDate.size()).isGreaterThanOrEqualTo(1);

        // 날짜별로 그룹화된 SHIPPING 주문 확인
        for (Map.Entry<LocalDate, OrderDto> entry : shippingOrdersByDate.entrySet()) {
            OrderDto shippingOrder = entry.getValue();
            assertThat(shippingOrder.getOrderStatus()).isEqualTo("SHIPPING");
        }
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 주문 조회 시 예외 발생")
    void testNonExistentEmailThrowsException() {
        // given
        String nonExistentEmail = "notfound@example.com";

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            orderService.getGroupedOrdersByEmail(nonExistentEmail);
        });
    }
}
