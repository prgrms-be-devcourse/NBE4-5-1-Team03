package com.shop.coffee.service;

import com.shop.coffee.order.dto.OrderDto;
import com.shop.coffee.order.entity.Order;
import com.shop.coffee.order.repository.OrderRepository;
import com.shop.coffee.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.shop.coffee.order.OrderStatus.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.shop.coffee.CoffeeApplication.class)
@ActiveProfiles("test")
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        Order order = new Order();
        order.setEmail("test@email.com");
        order.setAddress("테스트주소");
        order.setZipcode("1010101");
        order.setOrderStatus(RECEIVED);
        order.setTotalPrice(999999);
        orderRepository.save(order);
    }


    @Test
    @DisplayName("주문 단건 조회 테스트")
    @Transactional
    public void single_order_retrieve(){
        // given
        Order savedOrder = orderRepository.findAll().get(0);

        // when
        OrderDto result = orderService.getOrderById(savedOrder.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedOrder.getId());
        assertThat(result.getEmail()).isEqualTo("test@email.com");
        assertThat(result.getAddress()).isEqualTo("테스트주소");
        assertThat(result.getZipcode()).isEqualTo("1010101");
        assertThat(result.getOrderStatus()).isEqualTo("RECEIVED");
        assertThat(result.getTotalPrice()).isEqualTo(999999);

    }
}
